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

package juzu.impl.compiler;

import juzu.Response;
import juzu.impl.common.Formatting;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationException extends Exception {

  /** . */
  private List<CompilationError> errors;

  public CompilationException(List<CompilationError> errors) {
    this.errors = errors;
  }

  public List<CompilationError> getErrors() {
    return errors;
  }

  @Override
  public String toString() {
    return "CompilationException[" + errors + "]";
  }

  public Response.Error asResponse() {
    return new Response.Error((String)null) {
      @Override
      public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        for (CompilationError error : errors) {
          buffer.append(error).append("\n");
        }
        return buffer.toString();
      }
      @Override
      public String getHtmlMessage() {
        StringBuilder buffer = new StringBuilder();
        Formatting.renderErrors(buffer, errors);
        return buffer.toString();
      }
    };
  }
}
