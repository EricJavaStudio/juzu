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

package juzu.plugin.less;

import juzu.impl.compiler.CompilationError;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.common.Tools;
import juzu.plugin.less.impl.LessMetaModelPlugin;
import juzu.test.AbstractInjectTestCase;
import juzu.test.CompilerAssert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessPluginTestCase extends AbstractInjectTestCase {

  public LessPluginTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void testCompile() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less.compile");
    ca.assertCompile();
    File f = ca.getClassOutput().getPath("plugin", "less", "compile", "assets", "stylesheet.css");
    assertNotNull(f);
    assertTrue(f.exists());
  }

  @Test
  public void testFail() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less.fail");
    List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
    assertEquals(1, errors.size());
    assertEquals(LessMetaModelPlugin.COMPILATION_ERROR, errors.get(0).getCode());
    File f = ca.getSourcePath().getPath("plugin", "less", "fail", "package-info.java");
    assertEquals(f, errors.get(0).getSourceFile());
    f = ca.getClassOutput().getPath("plugin", "less", "fail", "assets", "stylesheet.css");
    assertNull(f);
  }

  @Test
  public void testNotFound() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less.notfound");
    List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
    assertEquals(1, errors.size());
    assertEquals(LessMetaModelPlugin.COMPILATION_ERROR, errors.get(0).getCode());
    File f = ca.getSourcePath().getPath("plugin", "less", "notfound", "package-info.java");
    assertEquals(f, errors.get(0).getSourceFile());
    f = ca.getClassOutput().getPath("plugin", "less", "notfound", "assets", "stylesheet.css");
    assertNull(f);
  }

  @Test
  public void testMinify() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less.minify");
    ca.assertCompile();
    File f = ca.getClassOutput().getPath("plugin", "less", "minify", "assets", "stylesheet.css");
    assertNotNull(f);
    assertTrue(f.exists());
    String s = Tools.read(f);
    assertFalse(s.contains(" "));
  }

  @Test
  public void testResolve() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less.resolve");
    ca.assertCompile();
    File f = ca.getClassOutput().getPath("plugin", "less", "resolve", "assets", "stylesheet.css");
    assertNotNull(f);
    assertTrue(f.exists());
  }

  @Test
  public void testCannotResolve() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less.cannotresolve");
    List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
    assertEquals(1, errors.size());
    assertEquals(LessMetaModelPlugin.COMPILATION_ERROR, errors.get(0).getCode());
    File f = ca.getSourcePath().getPath("plugin", "less", "cannotresolve", "package-info.java");
    assertEquals(f, errors.get(0).getSourceFile());
    f = ca.getClassOutput().getPath("plugin", "less", "cannotresolve", "assets", "stylesheet.css");
    assertNull(f);
  }

  @Test
  public void testMalformedPath() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less.malformedpath");
    List<CompilationError> errors = ca.formalErrorReporting(true).failCompile();
    assertEquals(1, errors.size());
    assertEquals(LessMetaModelPlugin.MALFORMED_PATH, errors.get(0).getCode());
    File f = ca.getSourcePath().getPath("plugin", "less", "malformedpath", "package-info.java");
    assertEquals(f, errors.get(0).getSourceFile());
    f = ca.getClassOutput().getPath("plugin", "less", "malformedpath", "assets", "stylesheet.css");
    assertNull(f);
  }

  @Test
  public void testAncestor() throws Exception {
    CompilerAssert<File, File> ca = compiler("plugin.less.ancestor");
    ca.assertCompile();
    File f = ca.getClassOutput().getPath("plugin", "less", "ancestor", "assets", "folder", "stylesheet.css");
    assertNotNull(f);
    assertTrue(f.exists());
  }
}
