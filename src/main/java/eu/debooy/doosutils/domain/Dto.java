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

import eu.debooy.doosutils.DoosConstants;
import eu.debooy.doosutils.DoosUtils;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;


/**
 * Data Transfer Object pattern.
 *
 * @author Marco de Booij
 */
public abstract class Dto implements Serializable {
  private static final  long  serialVersionUID  = 1L;

  private static final  Set<String> EXCLUDE_METHODS   =
      new HashSet<String>() {
        private static final  long  serialVersionUID  = 1L;
              {add("Class"); add("Logger");}};
  private static final  String      GET               = "get";
  private static final  String      LIKE              = "%";
  private static final  String      IS                = "is";

  private boolean checkNullDiff(Object object1, Object object2,
                                boolean modified) {
    if ((null != object1 && null == object2)
        || (null == object1 && null != object2)) {
      return true;
    }

    return modified;
  }

  public Logger getLogger() {
    return null;
  }

  private boolean isCollectionModified(Collection<?> collection,
                                       Collection<?> oldCollection) {
    if (null == collection) {
      return (null != oldCollection );
    }
    if (collection.equals(oldCollection)) {
      return true;
    }

    var objects     = new Object[collection.size()];
    objects         = collection.toArray(objects);
    var oldObjects  = new Object[oldCollection.size()];
    oldObjects      = oldCollection.toArray(oldObjects);
    var modified    = false;
    var i           = 0;
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

  private boolean isCollections(Object object1, Object object2) {
    return object1 instanceof Collection && object2 instanceof Collection;
  }

  private boolean isDtos(Object object1, Object object2) {
    return object1 instanceof Dto && object2 instanceof Dto;
  }

  public boolean isModified(Dto oldDto, boolean checkCollections) {
    if (null == oldDto) {
      return true;
    }

    var     modified  = false;
    Object  object1;
    Object  object2;
    try {
      for (var method : DoosUtils.findGetters(this.getClass().getMethods())) {
        object1   = method.invoke(this);
        object2   = method.invoke(oldDto);
        modified  = checkNullDiff(object1, object2, modified);
        if (null != object1 && null != object2) {
          if (isCollections(object1, object2)) {
            if (checkCollections) {
              modified |= isCollectionModified((Collection<?>) object1,
                                               (Collection<?>) object2);

            }
          } else if (isDtos(object1, object2)) {
            modified |= ((Dto) object1).isModified((Dto) object2, false);
          } else {
            modified |= !object1.equals(object2);
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
    var     filter    = new DoosFilter<T>();
    String  attribute;
    Object  waarde;

    for (var method : DoosUtils.findGetters(this.getClass().getMethods())) {
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
    var     sb        = new StringBuilder();
    String  attribute = null;
    Object  waarde    = null;

    sb.append(this.getClass().getSimpleName()).append(" (");
    for (var method : DoosUtils.findGetters(this.getClass().getMethods())) {
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
          sb.append(DoosConstants.NULL);
        }
      } catch (IllegalAccessException | IllegalArgumentException
               | InvocationTargetException e) {
        var logger  = getLogger();
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
