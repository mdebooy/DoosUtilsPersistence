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

  public final int compareTo(Sort obj) {
    return (property+"|"+order).compareTo(obj.property+"|"+obj.order);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    final Sort  other = (Sort) obj;
    if (order != other.order) {
      return false;
    }
    if (property != other.property) {
      return false;
    }

    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public final int hashCode() {
    final int prime     = 31;
    int       result    = super.hashCode();

    result  = prime * result + (null == order ? 0 : order.hashCode());
    result  = prime * result + (null == property ? 0 : property.hashCode());

    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public final String toString() {
    StringBuilder result  = new StringBuilder();

    result.append("property: ").append(property)
          .append(" - order: ").append(order);

    return result.toString();
  }
}
