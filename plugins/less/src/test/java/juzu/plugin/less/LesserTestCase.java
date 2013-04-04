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

import juzu.plugin.less.impl.lesser.Compilation;
import juzu.plugin.less.impl.lesser.Failure;
import juzu.plugin.less.impl.lesser.JSContext;
import juzu.plugin.less.impl.lesser.LessError;
import juzu.plugin.less.impl.lesser.Lesser;
import juzu.plugin.less.impl.lesser.URLLessContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(Parameterized.class)
public class LesserTestCase {

  @Parameterized.Parameters
  public static Collection<Object[]> configs() throws Exception {
    return Arrays.asList(new Object[][]{{new Lesser(JSContext.create())}});
  }

  /** . */
  private Lesser lesser;

  public LesserTestCase(Lesser lesser) {
    this.lesser = lesser;
  }

  @Test
  public void testSimple() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Compilation compilation = (Compilation)lesser.compile(context, "simple.less");
    Assert.assertEquals(".class {\n" +
      "  width: 2;\n" +
      "}\n", compilation.getValue());

    //
    compilation = (Compilation)lesser.compile(context, "simple.less", true);
    Assert.assertEquals(".class{width:2;}\n", compilation.getValue());
  }

  @Test
  public void testFail() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure ret = (Failure)lesser.compile(context, "fail.less");
    LinkedList<LessError> errors = ret.getErrors();
    Assert.assertEquals(1, errors.size());
    LessError error = errors.get(0);
    Assert.assertEquals("fail.less", error.src);
    Assert.assertEquals(1, error.line);
    Assert.assertEquals(8, error.column);
    Assert.assertEquals(8, error.index);
    Assert.assertEquals("Parse", error.type);
  }

  @Test
  public void testCannotResolveImport() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure failure = (Failure)lesser.compile(context, "cannotresolveimport.less");
    LinkedList<LessError> errors = failure.getErrors();
    Assert.assertEquals(1, errors.size());
    LessError error = errors.get(0);
    Assert.assertEquals(1, error.line);
    Assert.assertEquals(4, error.column);
    Assert.assertEquals(4, error.index);
    Assert.assertEquals(Collections.emptyList(), Arrays.asList(error.extract));
    Assert.assertEquals("Parse", error.type);
  }

  @Test
  public void testSeveralErrors() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure failure = (Failure)lesser.compile(context, "severalerrors1.less");
    LinkedList<LessError> errors = failure.getErrors();
    Assert.assertEquals(2, errors.size());
  }

  @Test
  public void testBootstrap() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/bootstrap/"));
    long time = -System.currentTimeMillis();
    Compilation compilation = (Compilation)lesser.compile(context, "bootstrap.less");
    time += System.currentTimeMillis();
    Assert.assertNotNull(compilation);
    System.out.println("Bootstrap parsed in " + time + "ms");
  }

  @Test
  public void testImport() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Compilation compilation = (Compilation)lesser.compile(context, "importer.less");
    Assert.assertEquals("a {\n" +
      "  width: 2px;\n" +
      "}\n", compilation.getValue());
  }

  @Test
  public void testUnresolableVariable() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure failure = (Failure)lesser.compile(context, "unresolvablevariable.less");
    LinkedList<LessError> errors = failure.getErrors();
    Assert.assertEquals(1, errors.size());
    LessError error = errors.get(0);
    Assert.assertEquals(1, error.line);
    Assert.assertEquals(17, error.column);
    Assert.assertEquals(17, error.index);
    Assert.assertEquals("Name", error.type);
  }

  @Test
  public void testExtract() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Failure failure = (Failure)lesser.compile(context, "extract.less");
    Assert.assertEquals(1, failure.getErrors().size());
    LessError error = failure.getErrors().get(0);
    Assert.assertEquals(2, error.line);
    String[] extract = error.extract;
    Assert.assertEquals(3, extract.length);
    Assert.assertEquals("// comment 1", extract[0]);
    Assert.assertEquals("a { width: + 1px }", extract[1]);
    Assert.assertEquals("// comment 2", extract[2]);
  }

  @Test
  public void testImportRelative() throws Exception {
    URLLessContext context = new URLLessContext(LesserTestCase.class.getClassLoader().getResource("lesser/test/"));
    Compilation compilation = (Compilation)lesser.compile(context, "relative.less");
    Assert.assertEquals("a {\n" +
        "  width: 2px;\n" +
        "}\n", compilation.getValue());
  }
}
