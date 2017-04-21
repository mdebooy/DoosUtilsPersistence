/**
 * Copyright 2009 Marco de Booij
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
package eu.debooy.doosutils;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * @author Marco de Booij
 */
public class Sort implements Serializable, Comparable<Sort> {
  private static final  long serialVersionUID = 1L;

  public static final String  ASC   = "asc";
  public static final String  DESC  = "desc";

  private final String  property;
  private final String  order;

  public Sort(String property, String order) {
    this.property = property;
    this.order    = order;
  }

  public final String getProperty() {
    return property;
  }

  public final String getOrder() {
    return order;
  }

  public final int compareTo(Sort andere) {
    return new CompareToBuilder().append(property, andere.property)
                                 .append(order, andere.order).toComparison();
  }

  public final boolean equals(Object object) {
    if (!(object instanceof Sort)) {
      return false;
    }
    if (object == this) {
      return true;
    }

    Sort  andere  = (Sort) object;
    return new EqualsBuilder().append(property, andere.property)
                              .append(order, andere.order).isEquals();
  }

  public final int hashCode() {
    return new HashCodeBuilder().append(property).append(order).toHashCode();
  }

  public final String toString() {
    StringBuilder result  = new StringBuilder();

    result.append("property: ").append(property)
          .append(" - order: ").append(order);

    return result.toString();
  }
}
