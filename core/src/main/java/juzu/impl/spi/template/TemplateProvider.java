/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.spi.template;

import java.io.IOException;
import java.io.Serializable;

/**
 * A provider for templating system.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class TemplateProvider<A extends Serializable> {

  public abstract Class<? extends TemplateStub> getTemplateStubType();

  public abstract A parse(CharSequence s) throws juzu.impl.spi.template.juzu.ast.ParseException;

  public abstract void process(ProcessContext context, Template<A> template);

  public abstract CharSequence emit(EmitContext context, A ast) throws IOException;

  public abstract String getSourceExtension();

  public abstract String getTargetExtension();

}