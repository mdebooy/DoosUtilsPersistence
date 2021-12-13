/**
 * Copyright 2012 Marco de Booij
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

import eu.debooy.doosutils.Filter;
import eu.debooy.doosutils.errorhandling.exception.IllegalArgumentException;
import eu.debooy.doosutils.errorhandling.exception.base.DoosLayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


/**
 * @author Marco de Booij
 *
 * @param <T>
 */
public class DoosFilter<T> implements CriteriaCommand<T> {
  private List<Filter>  filters = new ArrayList<>();

  public void addFilter(String element, Object waarde) {
    filters.add(new Filter(element, waarde));
  }

  public final List<Filter> getAll() {
    return filters;
  }

  private Predicate buildCriteria(CriteriaBuilder builder, Root<T> from,
                                  String element, Object waarde) {
    Predicate predicate	= null;
    if (null == waarde) {
      predicate = builder.isNull(from.get(element));
    } else {
      if (waarde instanceof String) {
        if (((String) waarde).contains("%")) {
          predicate =
              builder.like(builder.upper(from.<String>get(element)),
                                         ((String) waarde).toUpperCase());
        } else {
          predicate =
              builder.equal(builder.upper(from.<String>get(element)),
                                          ((String) waarde).toUpperCase());
        }
      } else if (waarde instanceof Date) {
        predicate = builder.equal(from.<Date>get(element), (waarde));
      } else if (waarde instanceof Number) {
        predicate = builder.equal(from.<Number>get(element), (waarde));
      } else if (waarde instanceof Boolean) {
        // Booleans overslaan
        predicate = null;
      } else {
        throw new IllegalArgumentException(DoosLayer.PERSISTENCE,
                                           "error.illegaltype");
      }
    }

    return predicate;
  }

  @Override
  public void execute(CriteriaBuilder builder, Root<T> from,
                      CriteriaQuery<T> query) {
    if (filters.isEmpty()) {
      return;
    }

    var j     = 0;
    var where = new Predicate[filters.size()];
    for (var i = 0; i < filters.size(); i++) {
      var filter    = filters.get(i);
      var predicaat = buildCriteria(builder, from, filter.getElement(),
                                          filter.getWaarde());
      if (null != predicaat) {
        where[j]  = predicaat;
        j++;
      }
    }

    query.where(Arrays.copyOf(where, j));
  }

  @Override
  public String toString() {
    var sb  = new StringBuilder();

    filters.forEach(filter -> {
      var waarde  = filter.getWaarde();
      sb.append(", ").append(filter.getElement());
      if (waarde instanceof String) {
        if (((String) waarde).contains("%")) {
          sb.append(" like ");
        } else {
          sb.append(" = ");
        }
        sb.append((String) waarde);
      } else if (waarde instanceof Date) {
        sb.append(" = ").append(waarde);
      } else if (waarde instanceof Number) {
        sb.append(" = ").append(waarde);
      } else {
        sb.append(" illegal argument ");
        if (null != waarde) {
          sb.append(waarde.getClass().getName());
        }
      }
    });

    return sb.toString().replaceFirst(", ", "");
  }
}
