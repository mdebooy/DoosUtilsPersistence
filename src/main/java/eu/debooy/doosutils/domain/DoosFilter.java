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
 * @param <T>
 */
public class DoosFilter<T> implements CriteriaCommand<T> {
  private List<Filter>  filters = new ArrayList<Filter>();

  /**
   * Voeg een conditie toe aan de filter.
   * 
   * @param element
   * @param waarde
   */
  public void addFilter(String element, Object waarde) {
    filters.add(new Filter(element, waarde));
  }

  /**
   * Geef alle filter elementen.
   * 
   * @return
   */
  public final List<Filter> getAll() {
    return filters;
  }

  /**
   * Maak de criteria.
   * 
   * @param builder
   * @param from
   * @param element
   * @param waarde
   */
  private Predicate buildCriteria(CriteriaBuilder builder, Root<T> from,
                                  String element, Object waarde) {
    if (null == waarde) {
      return builder.isNull(from.get(element));
    } else {
      if (waarde instanceof String) {
        if (((String) waarde).contains("%")) {
          return builder.like(builder.upper(from.<String>get(element)),
                                            ((String) waarde).toUpperCase());
        } else {
          return builder.equal(builder.upper(from.<String>get(element)),
                                             ((String) waarde).toUpperCase());
        }
      } else if (waarde instanceof Date) {
        return builder.equal(from.<Date>get(element), ((Date) waarde));
      } else if (waarde instanceof Number) {
        return builder.equal(from.<Number>get(element), ((Number) waarde));
      } else if (waarde instanceof Boolean) {
        // Booleans overslaan
        return null;
      } else {
        throw new IllegalArgumentException(DoosLayer.PERSISTENCE,
                                           "error.illegaltype");
      }
    }
  }

  /**
   * Maak de 'where clause'.
   * 
   * @param builder
   * @param from
   * @param query
   */
  @Override
  public void execute(CriteriaBuilder builder, Root<T> from,
                      CriteriaQuery<T> query) {
    if (filters.isEmpty()) {
      return;
    }

    int j = 0;
    Predicate[] where = new Predicate[filters.size()];
    for (int i = 0; i < filters.size(); i++) {
      Filter    filter    = filters.get(i);
      Predicate predicaat = buildCriteria(builder, from, filter.getElement(),
                                          filter.getWaarde());
      if (null != predicaat) {
        where[j]  = predicaat;
        j++;
      }
    }

    query.where(Arrays.copyOf(where, j));
  }

  /**
   * Maak een String van alle elementen van de filter.
   */
  @Override
  public String toString() {
    StringBuffer  sb  = new StringBuffer();

    for (Filter filter : filters) {
      Object  waarde  = filter.getWaarde();
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
        sb.append(waarde.getClass().getName());
      }
    }

    return sb.toString().replaceFirst(", ", "");
  }
}
