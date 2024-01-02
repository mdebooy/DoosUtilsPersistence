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
import eu.debooy.doosutils.PersistenceConstants;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONObject;
import org.slf4j.Logger;


/**
 * Data Transfer Object pattern.
 *
 * @author Marco de Booij
 */
public abstract class Dto implements Serializable {
  private static final  long  serialVersionUID  = 1L;

  private static final  Set<String> EXCLUDE_JSON     = new HashSet<>();
  private static final  Set<String> EXCLUDE_METHODS   = new HashSet<>();

  static {
    EXCLUDE_JSON.add("getClass");
    EXCLUDE_JSON.add("getId");

    EXCLUDE_METHODS.add("Class");
    EXCLUDE_METHODS.add("Logger");
  }

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
                                       Collection<?> oldCollection,
                                       boolean checkcollections) {
    if (!checkcollections) {
      return false;
    }

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
            modified |= isCollectionModified((Collection<?>) object1,
                                             (Collection<?>) object2,
                                             checkCollections);
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

  private void logException(String message, Exception e) {
    var logger  = getLogger();
    if (null != logger) {
      logger.error("{} {}: {}", message, e.getClass().getName(),
                                e.getLocalizedMessage());
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
      attribute = method.getName().substring(3);
      if (!method.getName().startsWith(PersistenceConstants.GET)
          || EXCLUDE_METHODS.contains(attribute)) {
        continue;
      }

      try {
        attribute = attribute.substring(0, 1).toLowerCase()
                    + attribute.substring(1);
        waarde    = method.invoke(this);
        if (!(waarde instanceof ArrayList)
            && DoosUtils.isNotBlankOrNull(waarde)) {
          if (like
              && waarde instanceof String
              && !((String) waarde).contains(PersistenceConstants.LIKE)) {
            waarde  = PersistenceConstants.LIKE + waarde
                        + PersistenceConstants.LIKE;
          }
          filter.addFilter(attribute, waarde);
        }
      } catch (IllegalAccessException | IllegalArgumentException
               | InvocationTargetException e) {
        logException("maakFilter", e);
      }
    }

    return filter;
  }

  public JSONObject toJSON() {
    var     json      = new JSONObject();
    String  attribute;

    for (var method : DoosUtils.findGetters(this.getClass().getMethods())) {
      if (EXCLUDE_JSON.contains(method.getName())) {
        continue;
      }

      if (method.getName().startsWith(PersistenceConstants.GET)) {
        attribute = method.getName().substring(3);
      } else {
        attribute = method.getName().substring(2);
      }

      try {
        attribute = attribute.substring(0, 1).toLowerCase()
                      + attribute.substring(1);
        waardeToJSON(method.invoke(this), json, attribute);
      } catch (IllegalAccessException | IllegalArgumentException
              | InvocationTargetException e) {
        logException("toJSON", e);
      }
    }

    return json;
  }

  @Override
  public String toString() {
    var     sb        = new StringBuilder();
    String  attribute;

    sb.append(this.getClass().getSimpleName()).append(" (");
    for (var method : DoosUtils.findGetters(this.getClass().getMethods())) {
      if (!method.getName().startsWith(PersistenceConstants.GET)
          && !method.getName().startsWith(PersistenceConstants.IS)) {
        continue;
      }

      if (method.getName().startsWith(PersistenceConstants.GET)) {
        attribute = method.getName().substring(3);
      } else {
        attribute = method.getName().substring(2);
      }

      try {
        attribute = attribute.substring(0, 1).toLowerCase()
                      + attribute.substring(1);
        sb.append(", ").append(attribute).append("=");
        waardeToString(method.invoke(this), sb);
      } catch (IllegalAccessException | IllegalArgumentException
               | InvocationTargetException e) {
        logException("toString", e);
      }
    }
    sb.append(")");

    return sb.toString().replaceFirst("\\(, ", "\\(");
  }

  private void waardeToJSON(Object waarde, JSONObject json, String attribute) {
    if (DoosUtils.isBlankOrNull(waarde)) {
      return;
    }

    if (waarde instanceof Dto) {
      // Geef enkel de naam van de andere DTO.
    } else {
      json.put(attribute, waarde);
    }
  }

  private void waardeToString(Object waarde, StringBuilder sb) {
    if (null == waarde) {
      sb.append(DoosConstants.NULL);
      return;
    }

    if (waarde instanceof Dto) {
      // Geef enkel de naam van de andere DTO.
      sb.append("<").append(waarde.getClass().getSimpleName())
        .append(">");
    } else {
      sb.append("[").append(waarde.toString()).append("]");
    }
  }
}
