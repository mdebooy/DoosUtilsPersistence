/**
 * Copyright 2011 Marco de Booij
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.debooy.doosutils.domain;

import eu.debooy.doosutils.DoosUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;


/**
 * Data Transfer Object pattern.
 * 
 * @author Marco de Booij
 */
public abstract class Dto implements Serializable {
  private static final  long  serialVersionUID  = 1L;

  public static final String[] GET_METHODS_PREFIXES = {"get", "is"};

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
   * @return
   */
  public Logger getLogger() {
    return null;
  }

  /**
   * Test om te zien of de Collection van DTOs gewijzigd is.
   * 
   * @param collection
   * @param oldCollection
   * @return
   */
  private boolean isCollectionModified(Collection<?> collection,
                                       Collection<?> oldCollection) {
    if (null != collection && null == oldCollection) {
      return true;
    } else if ((null == collection && null != oldCollection)
        || (null == collection && null == oldCollection)) {
      return false;
    } else if (collection.size() != oldCollection.size()) {
      return true;
    }

    Object[]  objects     = new Object[collection.size()];
    objects     = collection.toArray(objects);
    Object[]  oldObjects  = new Object[oldCollection.size()];
    oldObjects  = oldCollection.toArray(oldObjects);
    boolean   modified    = false;
    for (int i = 0; i < objects.length; i++) {
      if (objects[i] instanceof Dto && oldObjects[i] instanceof Dto) {
        modified |= ((Dto) objects[i]).isModified((Dto) oldObjects[i], false);
      } else {
        modified |= !objects[i].equals(oldObjects[i]);
      }
      if (modified) {
        break;
      }
    }

    return modified;
  }

  /**
   * Test om te zien of de DTO gewijzigd is.
   * 
   * @param oldDto
   * @param checkCollections
   * @return
   */
  public boolean isModified(Dto oldDto, boolean checkCollections) {
    if (null == oldDto) {
      return true;
    }

    Object[]  args      = null;
    boolean   modified  = false;
    Object    value1;
    Object    value2;
    try {
      for (Method method : findGetters()) {
        value1  = method.invoke(this, args);
        value2  = method.invoke(oldDto, args);
        if ((null != value1 && null == value2)
            || (null == value1 && null != value2)) {
          modified = true;
        }
        if (null != value1 && null != value2) {
          if (value1 instanceof Collection
                  && value2 instanceof Collection) {
            if (checkCollections) {
              modified |= isCollectionModified((Collection<?>) value1,
                                               (Collection<?>) value2);
  
            }
          } else if (value1 instanceof Dto && value2 instanceof Dto) {
            modified |= ((Dto) value1).isModified((Dto) value2, false);
          } else {
            modified |= !value1.equals(value2);
          }
        }
        if (modified) {
          break;
        }
      }
      return modified;
    } catch (InvocationTargetException e) {
      return true;
    } catch (IllegalArgumentException e) {
      return true;
    } catch (IllegalAccessException e) {
      return true;
    }
  }

  /**
   * Maak een DoosFilter die gebaseerd is op de DTO.
   * 
   * @param <T>
   * 
   * @return
   */
  public <T> DoosFilter<T> makeFilter() {
    DoosFilter<T> filter    = new DoosFilter<T>();
    String        attribute = null;
    Object        waarde    = null;
    Object[]      arg       = null;

    for (Method method : findGetters()) {
      if (method.getName().startsWith("get")) {
        try {
          attribute = method.getName().substring(3);
          if (!attribute.equals("Class")
              && !attribute.equals("Logger")) {
            attribute = attribute.substring(0, 1).toLowerCase()
                        + attribute.substring(1);
            waarde    = method.invoke(this, arg);
            if (!(waarde instanceof ArrayList)) {
              if (DoosUtils.isNotBlankOrNull(waarde)) {
                filter.addFilter(attribute, waarde);
              }
            }
          }
        } catch (IllegalArgumentException e) {
          Logger  logger  = getLogger();
          if (null != logger) {
            logger.error("makeFilter IllegalArgumentException: "
                         + e.getMessage());
          }
        } catch (IllegalAccessException e) {
          Logger  logger  = getLogger();
          if (null != logger) {
            logger.error("makeFilter IllegalAccessException: "
                         + e.getMessage());
          }
        } catch (InvocationTargetException e) {
          Logger  logger  = getLogger();
          if (null != logger) {
            logger.error("makeFilter InvocationTargetException: "
                         + e.getMessage());
          }
        }
      }
    }

    return filter;
  }

  /**
   * Maak een String van alle attributen die via een getter te benaderen zijn.
   */
  @Override
  public String toString() {
    StringBuffer  sb        = new StringBuffer();
    String        attribute = null;
    Object        waarde    = null;
    Object[]      arg       = null;

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
        waarde = method.invoke(this, arg);
        if (null != waarde) {
          if (waarde instanceof Dto) {
            // Geef enkel de naam van andere DTOs.
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
