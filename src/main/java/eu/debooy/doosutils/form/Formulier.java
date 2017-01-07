/**
 * Copyright 2016 Marco de Booij
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.debooy.doosutils.form;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;


/**
 * @author Marco de Booij
 */
public class Formulier implements Serializable {
  private static final  long  serialVersionUID  = 1L;

  protected boolean gewijzigd = false;

  private static final  String[]  GET_METHODS_PREFIXES  = {"get", "is"};

  /**
   * Zoek alle 'getters'.
   * 
   * @return
   */
  public Method[] findGetters() {
    List<Method>  getters   = new ArrayList<Method>();
    Method[]      methodes  = this.getClass().getMethods();
    for (Method method : methodes) {
      for (String prefix : GET_METHODS_PREFIXES) {
        if (method.getName().startsWith(prefix)) {
          if (method.getParameterTypes() == null
              || method.getParameterTypes().length == 0) {
            getters.add(method);
          }
          break;
        }
      }
    }
    methodes = new Method[getters.size()];

    return getters.toArray(methodes);
  }

  /**
   * Geef de LOGGER van de extended class.
   * 
   * @return Logger
   */
  public Logger getLogger() {
    return null;
  }

  /**
   * Is er iets gewijzigd?
   * 
   * @return
   */
  public boolean isGewijzigd() {
    return gewijzigd;
  }

  /**
   * Maak een String van alle attributen die via een getter te benaderen zijn.
   */
  public String toString() {
    StringBuilder sb        = new StringBuilder();
    String        attribute = null;
    Object        waarde    = null;

    sb.append(this.getClass().getSimpleName()).append(" (");
    for (Method method : findGetters()) {
      try {
        if (method.getName().startsWith("get")) {
          attribute = method.getName().substring(3);
        } else if (method.getName().startsWith("is")) {
          attribute = method.getName().substring(2);
        } else {
          continue;
        }
        attribute = attribute.substring(0, 1).toLowerCase()
                    + attribute.substring(1);
        sb.append(", ").append(attribute).append("=");
        waarde = method.invoke(this);
        if (null != waarde) {
          if (waarde instanceof Formulier) {
            // Geef enkel de naam van andere Formulieren.
            sb.append("<").append(waarde.getClass().getSimpleName())
              .append(">");
          } else {
            sb.append("[").append(waarde.toString()).append("]");
          }
        } else {
          sb.append("<null>");
        }
      } catch (IllegalArgumentException e) {
        Logger  logger  = getLogger();
        if (null != logger) {
          logger.error("toString IllegalArgumentException: " + e.getMessage());
        }
      } catch (IllegalAccessException e) {
        Logger  logger  = getLogger();
        if (null != logger) {
          logger.error("toString IllegalAccessException: " + e.getMessage());
        }
      } catch (InvocationTargetException e) {
        Logger  logger  = getLogger();
        if (null != logger) {
          logger.error("toString InvocationTargetException: " + e.getMessage());
        }
      }
    }
    sb.append(")");

    return sb.toString().replaceFirst("\\(, ", "\\(");
  }
}
