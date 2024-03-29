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

import eu.debooy.doosutils.DoosConstants;
import eu.debooy.doosutils.DoosUtils;
import eu.debooy.doosutils.PersistenceConstants;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;


/**
 * @author Marco de Booij
 */
public class Formulier implements Serializable {
  private static final  long  serialVersionUID  = 1L;

  public Logger getLogger() {
    return null;
  }

  @Override
  public String toString() {
    var     sb        = new StringBuilder();

    sb.append(this.getClass().getSimpleName()).append(" (");

    DoosUtils.findGetters(this.getClass().getMethods()).forEach(method -> {
      String  attribute;
      Object  waarde;

      if (method.getName().startsWith(PersistenceConstants.GET)) {
        attribute = method.getName().substring(3);
      } else {
        attribute = method.getName().substring(2);
      }
      try {
        attribute = attribute.substring(0, 1).toLowerCase()
                      + attribute.substring(1);
        sb.append(", ").append(attribute).append("=");
        waarde = method.invoke(this);
        if (null != waarde) {
          if (waarde instanceof Formulier) {
            // Geef enkel de naam van het andere Formulier.
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
          logger.error("toString {}: {}", e.getClass().getName(),
                                          e.getLocalizedMessage());
        }
      }
    });
    sb.append(")");

    return sb.toString().replaceFirst("\\(, ", "\\(");
  }
}
