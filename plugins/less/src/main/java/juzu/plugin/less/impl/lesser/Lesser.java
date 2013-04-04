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

package juzu.plugin.less.impl.lesser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Lesser {

  /** . */
  private static final ThreadLocal<LessContext> current = new ThreadLocal<LessContext>();

  /** . */
  private static final ThreadLocal<Result> currentResult = new ThreadLocal<Result>();

  /** . */
  private final JSContext engine;

  public class Bridge {
    public String load(String name) {
      return current.get().load(name);
    }

    public void failure(String src, int line, int column, int index, String message, String type, String[] extract) {
      Failure failure = (Failure)currentResult.get();
      if (failure == null) {
        currentResult.set(failure = new Failure());
      }
      failure.errors.add(new LessError(src, line, column, index, message, type, extract));
    }

    public void compilation(String result) {
      currentResult.set(new Compilation(result));
    }
  }

  public Lesser(JSContext jsContext) throws Exception {
    InputStream lessIn = getClass().getResourceAsStream("less.js");

    //
    ByteArrayOutputStream baos = append(lessIn, new ByteArrayOutputStream());

    //
    jsContext.put("bridge", new Bridge());
    jsContext.eval("load = function(name) { return '' + bridge.load(name); }");
    jsContext.eval("failure = function(src, line, column, index, message, type, extract) { bridge.failure(src, line, column, index, message, type, extract); }");
    jsContext.eval("compilation = function(stylesheet) { bridge.compilation(stylesheet); }");
    jsContext.put("window", "{}");

    //
    jsContext.eval(baos.toString());

    //
    this.engine = jsContext;
  }

  public Result compile(LessContext context, String name) throws Exception {
    return compile(context, name, false);
  }

  public Result compile(LessContext context, String name, boolean compress) throws Exception {
    current.set(context);
    try {
      engine.invokeFunction("parse", name, compress);
      return currentResult.get();
    }
    finally {
      current.set(null);
      currentResult.set(null);
    }
  }

  static <O extends OutputStream> O append(InputStream in, O out) throws IOException {
    byte[] buffer = new byte[256];
    for (int l = in.read(buffer);l != -1;l = in.read(buffer)) {
      out.write(buffer, 0, l);
    }
    return out;
  }
}
