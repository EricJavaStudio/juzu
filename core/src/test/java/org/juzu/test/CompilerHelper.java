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

package org.juzu.test;

import org.juzu.impl.application.JuzuProcessor;
import org.juzu.impl.compiler.CompilationError;
import org.juzu.impl.compiler.Compiler;
import org.juzu.impl.spi.fs.ReadFileSystem;
import org.juzu.impl.spi.fs.ReadWriteFileSystem;
import org.juzu.impl.spi.fs.ram.RAMFileSystem;
import org.juzu.impl.spi.fs.ram.RAMPath;
import org.juzu.test.request.MockApplication;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilerHelper<I, O>
{

   public static <I> CompilerHelper<I, RAMPath> create(ReadFileSystem<I> input)
   {
      try
      {
         return new CompilerHelper<I, RAMPath>(input, new RAMFileSystem());
      }
      catch (IOException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   /** . */
   private ReadFileSystem<I> input;

   /** . */
   private ReadWriteFileSystem<O> output;

   /** . */
   private ClassLoader cl;

   /** . */
   private Compiler<?, ?> compiler;

   public CompilerHelper(ReadFileSystem<I> input, ReadWriteFileSystem<O> output)
   {
      this.input = input;
      this.output = output;
   }

   public ReadWriteFileSystem<O> getOutput()
   {
      return output;
   }

   public List<CompilationError> failCompile()
   {
      try
      {
         Compiler<I, O> compiler = new org.juzu.impl.compiler.Compiler<I, O>(input, output);
         compiler.addAnnotationProcessor(new JuzuProcessor());
         List<CompilationError> errors = compiler.compile();
         AbstractTestCase.assertTrue("Was expecting compilation to fail", errors.size() > 0);
         return errors;
      }
      catch (IOException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public MockApplication<?> application()
   {
      try
      {
         ClassLoader classLoader = new URLClassLoader(new URL[]{getOutput().getURL()}, Thread.currentThread().getContextClassLoader());
         MockApplication<O> app = new MockApplication<O>(getOutput(), classLoader);
         app.init();
         return app;
      }
      catch (Exception e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public Compiler<?, ?> assertCompile()
   {
      try
      {
         Compiler<I, O> compiler = new org.juzu.impl.compiler.Compiler<I, O>(input, output);
         compiler.addAnnotationProcessor(new JuzuProcessor());
         AbstractTestCase.assertEquals(Collections.<CompilationError>emptyList(), compiler.compile());
         cl = new URLClassLoader(new URL[]{output.getURL()}, Thread.currentThread().getContextClassLoader());
         return compiler;
      }
      catch (IOException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }

   public Class<?> assertClass(String className)
   {
      try
      {
         return cl.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         throw AbstractTestCase.failure(e);
      }
   }
}
