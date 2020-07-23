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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;


/**
 * Data Transfer Object pattern.
 *
 * @author Marco de Booij
 */
public abstract class Dto implements Serializable {
  private static final  long  serialVersionUID  = 1L;

  private static final  Set<String> EXCLUDE_METHODS       =
      new HashSet<String>() {
        private static final  long  serialVersionUID      = 1L;
              {add("Class"); add("Logger");}};
  private static final  String      GET                   = "get";
  private static final  String[]    GET_METHODS_PREFIXES  = {"get", "is"};
  private static final  String      LIKE                  = "%";
  private static final  String      IS                    = "is";
  private static final  String      NULL                  = "<null>";

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

  public Logger getLogger() {
    return null;
  }

  private boolean isCollectionModified(Collection<?> collection,
                                       Collection<?> oldCollection) {
    if (null == collection) {
      if (null == oldCollection ) {
        return false;
      } else {
        return true;
      }
    }
    if (collection.equals(oldCollection)) {
      return true;
    }

    Object[]  objects     = new Object[collection.size()];
    objects     = collection.toArray(objects);
    Object[]  oldObjects  = new Object[oldCollection.size()];
    oldObjects  = oldCollection.toArray(oldObjects);
    boolean   modified    = false;
    int       i           = 0;
    while (i < objects.length && !modified) {
      if (objects[i] instanceof Dto && oldObjects[i] instanceof Dto) {
        modified |= ((Dto) objects[i]).isModified((Dto) oldObjects[i], false);
      } else {
        modified |= !objects[i].equals(oldObjects[i]);
      }
      i++;
    }

    return modified;
  }

  public boolean isModified(Dto oldDto, boolean checkCollections) {
    if (null == oldDto) {
      return true;
    }

    boolean   modified  = false;
    Object    value1;
    Object    value2;
    try {
      for (Method method : findGetters()) {
        value1  = method.invoke(this);
        value2  = method.invoke(oldDto);
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
    } catch (IllegalAccessException | IllegalArgumentException
             | InvocationTargetException e) {
      return true;
    }
  }

  public <T> DoosFilter<T> makeFilter() {
    return makeFilter(false);
  }

  public <T> DoosFilter<T> makeFilter(boolean like) {
    DoosFilter<T> filter    = new DoosFilter<T>();
    String        attribute = null;
    Object        waarde    = null;

    for (Method method : findGetters()) {
      if (method.getName().startsWith(GET)) {
        try {
          attribute = method.getName().substring(3);
          if (!EXCLUDE_METHODS.contains(attribute)) {
            attribute = attribute.substring(0, 1).toLowerCase()
                        + attribute.substring(1);
            waarde    = method.invoke(this);
            if (!(waarde instanceof ArrayList)
                && DoosUtils.isNotBlankOrNull(waarde)) {
              if (like
                  && waarde instanceof String
                  && !((String) waarde).contains(LIKE)) {
                waarde  = LIKE + waarde + LIKE;
              }
              filter.addFilter(attribute, waarde);
            }
          }
        } catch (IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException e) {
          Logger  logger  = getLogger();
          if (null != logger) {
            logger.error("makeFilter" + e.getClass().getName() + ": "
                         + e.getMessage());
          }
        }
      }
    }

    return filter;
  }

  @Override
  public String toString() {
    StringBuilder sb        = new StringBuilder();
    String        attribute = null;
    Object        waarde    = null;

    sb.append(this.getClass().getSimpleName()).append(" (");
    for (Method method : findGetters()) {
      try {
        if (method.getName().startsWith(GET)) {
          attribute = method.getName().substring(3);
        } else if (method.getName().startsWith(IS)) {
          attribute = method.getName().substring(2);
        } else {
          continue;
        }
        attribute = attribute.substring(0, 1).toLowerCase()
                    + attribute.substring(1);
        sb.append(", ").append(attribute).append("=");
        waarde = method.invoke(this);
        if (null != waarde) {
          if (waarde instanceof Dto) {
            // Geef enkel de naam van andere DTOs.
            sb.append("<").append(waarde.getClass().getSimpleName())
              .append(">");
          } else {
            sb.append("[").append(waarde.toString()).append("]");
          }
        } else {
          sb.append(NULL);
        }
      } catch (IllegalAccessException | IllegalArgumentException
               | InvocationTargetException e) {
        Logger  logger  = getLogger();
        if (null != logger) {
          logger.error("toString" + e.getClass().getName() + ": "
                  + e.getMessage());
        }
      }
    }
    sb.append(")");

    return sb.toString().replaceFirst("\\(, ", "\\(");
  }
}
