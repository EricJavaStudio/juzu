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

import juzu.impl.common.Logger;
import juzu.impl.common.Tools;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class BaseProcessor extends AbstractProcessor {

  /** . */
  private static final String lineSep = System.getProperty("line.separator");

  /** . */
  private final static ThreadLocal<StringBuilder> currentLog = new ThreadLocal<StringBuilder>();

  /** . */
  private static final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>() {
    @Override
    protected DateFormat initialValue() {
      return new SimpleDateFormat("h:mm:ss:SSS");
    }
  };

  /** . */
  private static final Map<String, Logger> loggers = new HashMap<String, Logger>();

  /** . */
  private static final Logger logger = getLogger(BaseProcessor.class);

  public static Logger getLogger(Class<?> type) {
    String key = type.getName();
    final String name = type.getSimpleName();
    Logger logger = loggers.get(key);
    if (logger == null) {
      logger = new Logger() {
        public void log(CharSequence msg) {
          BaseProcessor.log(name, msg);
        }

        public void log(CharSequence msg, Throwable t) {
          BaseProcessor.log(name, msg, t);
        }
      };
      loggers.put(key, logger);
    }
    return logger;
  }

  private static void log(String name, CharSequence msg) {
    String s = format.get().format(new Date());
    StringBuilder sb = currentLog.get();
    if (sb != null) {
      sb.append(s).append(" ").append("[").append(name).append("] ").append(msg).append(lineSep);
    }
  }

  private static void log(String name, CharSequence msg, Throwable t) {
    StringWriter buffer = new StringWriter();
    t.printStackTrace(new PrintWriter(buffer));
    log(name, msg);
    StringBuilder sb = currentLog.get();
    if (sb != null) {
      sb.append(buffer);
    }
  }

  /** Controls how error are reported. */
  private boolean formalErrorReporting;

  /** . */
  private ProcessingContext context;

  protected BaseProcessor() {
    this.formalErrorReporting = false;
  }

  public final boolean getFormalErrorReporting() {
    return formalErrorReporting;
  }

  public final ProcessingContext getContext() {
    return context;
  }

  @Override
  public final Set<String> getSupportedOptions() {
    Set<String> options = super.getSupportedOptions();
    HashSet<String> our = new HashSet<String>(options);
    our.add("juzu.error_reporting");
    return our;
  }

  @Override
  public final void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    //
    this.currentLog.set(new StringBuilder());

    //
    this.formalErrorReporting = "formal".equalsIgnoreCase(processingEnv.getOptions().get("juzu.error_reporting"));
    this.context = new ProcessingContext(processingEnv);

    //
    doInit(context);
  }

  @Override
  public final Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton("*");
  }

  @Override
  public final SourceVersion getSupportedSourceVersion() {
    return javax.lang.model.SourceVersion.RELEASE_6;
  }

  /**
   * Perform the processor initialization.
   *
   * @param context the processing context
   */
  protected void doInit(ProcessingContext context) {
  }

  @Override
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {
      doProcess(annotations, roundEnv);
    }
    catch (Exception e) {
      if (e instanceof ProcessingException) {
        ProcessingException ce = (ProcessingException)e;
        Element element = ce.getElement();
        AnnotationMirror annotation = ce.getAnnotation();

        //
        StringBuilder msg = new StringBuilder();

        for (Message cm : ce) {
          msg.setLength(0);
          MessageCode code = cm.getCode();
          String[] args = cm.getArguments();
          if (formalErrorReporting) {
            cm.format(msg, true);
          }
          else {
            try {
              new Formatter(msg).format(Locale.getDefault(), code.getMessage(), (Object[])args).flush();
            }
            catch (Exception e1) {
              e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
          }

          // Log error
          StringWriter writer = new StringWriter();
          if (element == null) {
            writer.append("Compilation error: ");
          }
          else if (annotation == null) {
            writer.
              append("Compilation error for element ").
              append(element.toString()).append(": ");
          }
          else {
            writer.
              append("Compilation error for element ").
              append(element.toString()).
              append(" at annotation ").
              append(annotation.toString()).append(": ");
          }
          writer.append(msg).append("\n");
          e.printStackTrace(new PrintWriter(writer));
          logger.log(writer.getBuffer());

          // Report to tool
          context.report(Diagnostic.Kind.ERROR, msg, element, annotation, null);
        }
      }
      else {
        String msg;
        if (e.getMessage() == null) {
          msg = "Exception : " + e.getClass().getName();
        }
        else {
          msg = e.getMessage();
        }

        // Log error
        StringWriter writer = new StringWriter();
        writer.append("Compilation error: ");
        writer.append(msg).append("\n");
        e.printStackTrace(new PrintWriter(writer));
        logger.log(writer.getBuffer());

        // Report to tool
        context.report(Diagnostic.Kind.ERROR, msg, null, null, null);
      }
    }
    finally {
      if (roundEnv.processingOver()) {
        String t = currentLog.get().toString();
        currentLog.set(new StringBuilder());

        //
        if (t.length() > 0) {
          String s = null;
          InputStream in = null;
          try {
            FileObject file = context.getResource(StandardLocation.SOURCE_OUTPUT, "juzu", "processor.log");
            in = file.openInputStream();
            s = Tools.read(in, Tools.UTF_8);
          }
          catch (Exception ignore) {
          }
          finally {
            Tools.safeClose(in);
          }
          OutputStream out = null;
          try {
            FileObject file = context.createResource(StandardLocation.SOURCE_OUTPUT, "juzu", "processor.log");
            out = file.openOutputStream();
            if (s != null) {
              out.write(s.getBytes(Tools.UTF_8));
            }
            out.write(t.getBytes(Tools.UTF_8));
          }
          catch (Exception ignore) {
          }
          finally {
            Tools.safeClose(out);
          }
        }
      }
    }

    //
    return false;
  }

  protected abstract void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessingException;
}
