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

import eu.debooy.doosutils.Sort;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;


/**
 * @author Marco de Booij
 *
 * @param <T>
 */
public class DoosSort<T> implements CriteriaCommand<T> {
  private final List<Sort>  sorts = new ArrayList<>();

  public void addSort(String property, String order) {
    sorts.add(new Sort(property, order));
  }

  @Override
  public void execute(CriteriaBuilder builder, Root<T> from,
                      CriteriaQuery<T> query) {
    if (sorts.isEmpty()) {
      return;
    }

    var orderBy = new Order[sorts.size()];
    for (var i = 0; i < sorts.size(); i++) {
      var sort  = sorts.get(i);
      if (sort.getOrder().equals(Sort.ASC)) {
        orderBy[i]  = builder.asc(from.get(sort.getProperty()));
      } else {
        orderBy[i]  = builder.desc(from.get(sort.getProperty()));
      }
    }

    query.orderBy(orderBy);
  }

  public final List<Sort> getAll() {
    return sorts;
  }

  @Override
  public String toString() {
    var sb  = new StringBuilder();

    sorts.forEach(sort ->
      sb.append(", ").append(sort.getProperty())
        .append(" ").append(sort.getOrder()));

    return sb.toString().replaceFirst(", ", "");
  }
}
