/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package juzu.impl.plugin.asset;

import juzu.asset.AssetLocation;
import juzu.impl.common.Name;
import juzu.impl.common.Path;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModel;
import juzu.impl.plugin.application.metamodel.ApplicationMetaModelPlugin;
import juzu.impl.metamodel.AnnotationKey;
import juzu.impl.metamodel.AnnotationState;
import juzu.impl.common.JSON;
import juzu.impl.compiler.ProcessingContext;
import juzu.plugin.asset.Assets;

import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetMetaModelPlugin extends ApplicationMetaModelPlugin {

  public AssetMetaModelPlugin() {
    super("asset");
  }

  @Override
  public Set<Class<? extends java.lang.annotation.Annotation>> init(ProcessingContext env) {
    return Collections.<Class<? extends java.lang.annotation.Annotation>>singleton(Assets.class);
  }

  @Override
  public void init(ApplicationMetaModel metaModel) {
    metaModel.addChild(AssetsMetaModel.KEY, new AssetsMetaModel());
  }

  @Override
  public void processAnnotationAdded(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState added) {
    if (metaModel.getHandle().equals(key.getElement())) {
      String location = (String)added.get("location");
      List<Map<String, Object>> value = (List<Map<String, Object>>)added.get("value");
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      assetsMetaModel.removeAssets("asset");
      for (Map<String, Object> asset : value) {
        String assetId = (String)asset.get("id");
        List<String> assetValue = (List<String>)asset.get("value");
        List<String> assetDepends = (List<String>)asset.get("depends");
        String assetLocation = (String)asset.get("location");
        if (assetLocation == null) {
          assetLocation = location;
        }
        assetsMetaModel.addAsset(new Asset(assetId, "asset", assetValue, assetDepends, assetLocation));
      }
    }
  }

  @Override
  public void processAnnotationRemoved(ApplicationMetaModel metaModel, AnnotationKey key, AnnotationState removed) {
    if (metaModel.getHandle().equals(key.getElement())) {
      AssetsMetaModel assetsMetaModel = metaModel.getChild(AssetsMetaModel.KEY);
      assetsMetaModel.removeAssets("asset");
    }
  }

  @Override
  public void prePassivate(ApplicationMetaModel metaModel) {
    ProcessingContext context = metaModel.getProcessingContext();
    if(!context.isCopyFromSourcesExternallyManaged()) {
      AssetsMetaModel annotation = metaModel.getChild(AssetsMetaModel.KEY);
      Iterator<Asset> assets = annotation.getAssets().iterator();
      if (assets.hasNext()) {
        while (assets.hasNext()) {
          Asset asset = assets.next();
          if (asset.location == null || AssetLocation.APPLICATION.equals(AssetLocation.safeValueOf(asset.location))) {
            for (String value : asset.value) {
              Path path = Path.parse(value);
              if (path.isRelative()) {
                context.info("Found classpath asset to copy " + value);
                Name qn = metaModel.getHandle().getPackageName().append("assets");
                Path.Absolute absolute = qn.resolve(path);
                FileObject src = context.resolveResourceFromSourcePath(metaModel.getHandle(), absolute);
                if (src != null) {
                  URI srcURI = src.toUri();
                  context.info("Found asset " + absolute + " on source path " + srcURI);
                  InputStream in = null;
                  OutputStream out = null;
                  try {
                    FileObject dst = context.getResource(StandardLocation.CLASS_OUTPUT, absolute);
                    if (dst == null || dst.getLastModified() < src.getLastModified()) {
                      in = src.openInputStream();
                      dst = context.createResource(StandardLocation.CLASS_OUTPUT, absolute, context.get(metaModel.getHandle()));
                      context.info("Copying asset from source path " + srcURI + " to class output " + dst.toUri());
                      out = dst.openOutputStream();
                      Tools.copy(in, out);
                    } else {
                      context.info("Found up to date related asset in class output for " + srcURI);
                    }
                  }
                  catch (IOException e) {
                    context.info("Could not copy asset " + path + " ", e);
                  }
                  finally {
                    Tools.safeClose(in);
                    Tools.safeClose(out);
                  }
                } else {
                  context.info("Could not find asset " + absolute + " on source path");
                }
              }
            }
          }
        }
      }
    }
  }

  @Override
  public JSON getDescriptor(ApplicationMetaModel application) {
    AssetsMetaModel assetsMetaModel = application.getChild(AssetsMetaModel.KEY);
    Iterator<Asset> assets = assetsMetaModel.getAssets().iterator();
    if (assets.hasNext()) {
      JSON json = new JSON();
      List<JSON> list = new ArrayList<JSON>();
      while (assets.hasNext()) {
        list.add(assets.next().getJSON());
      }
      json.set("assets", list);
      json.set("package", "assets");
      return json;
    } else {
      return null;
    }
  }
}
