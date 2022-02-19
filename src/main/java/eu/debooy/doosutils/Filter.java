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
public class Filter implements Serializable {
  private static final long serialVersionUID = 1L;

  private final           String  element;
  private final transient Object  waarde;

  public Filter(String element, Object waarde) {
    this.element  = element;
    this.waarde   = waarde;
  }

  public final String getElement() {
    return element;
  }

  public final Object getWaarde() {
    return waarde;
  }

  public final int compareTo(Filter filter) {
    return new CompareToBuilder().append(element, filter.element)
                                 .append(waarde, filter.waarde)
                                 .toComparison();
  }

  @Override
  public final boolean equals(Object object) {
    if (!(object instanceof Filter)) {
      return false;
    }

    var filter  = (Filter) object;
    return new EqualsBuilder().append(element, filter.element)
                              .append(waarde, filter.waarde).isEquals();

  }

  @Override
  public final int hashCode() {
    return new HashCodeBuilder().append(element).append(waarde)
                                .toHashCode();
  }

  @Override
  public final String toString() {
    var result  = new StringBuilder();

    result.append("element: ").append(element)
          .append(" - waarde: ").append(waarde);

    return result.toString();
  }
}
