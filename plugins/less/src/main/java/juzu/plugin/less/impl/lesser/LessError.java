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

import java.util.Arrays;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class LessError extends Result {

  /** . */
  public final String src;

  /** . */
  public final int line;

  /** . */
  public final int column;

  /** . */
  public final int index;

  /** . */
  public final String message;

  /** . */
  public final String type;

  /** . */
  public final String[] extract;

  public LessError(String src, int line, int column, int index, String message, String type, String[] extract) {
    this.src = src;
    this.line = line;
    this.column = column;
    this.index = index;
    this.message = message;
    this.type = type;
    this.extract = extract;
  }

  @Override
  public String toString() {
    return "Failure[src=" + src + ",line=" + line + ",column=" + column + ",index=" + index + ",message=" + message + ",type=" +
      type + ",extract=" + Arrays.asList(extract) + "]";
  }
}
