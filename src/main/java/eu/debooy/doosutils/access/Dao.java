/**
 * Copyright (c) 2012 Marco de Booij
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
package eu.debooy.doosutils.access;

import eu.debooy.doosutils.domain.DoosFilter;
import eu.debooy.doosutils.domain.DoosSort;
import eu.debooy.doosutils.domain.Dto;
import eu.debooy.doosutils.errorhandling.exception.DuplicateObjectException;
import eu.debooy.doosutils.errorhandling.exception.ObjectNotFoundException;
import eu.debooy.doosutils.errorhandling.exception.base.DoosLayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;


/**
 * Data Access Object pattern.
 *
 * @author Marco de Booij
 *
 * @param <T>
 */
public abstract class Dao<T extends Dto> {
  protected abstract  EntityManager getEntityManager();

  private final Class<T>  dto;

  protected Dao(Class<T> dto) {
    this.dto  = dto;
  }

  public T create(T dto) {
    try {
      getEntityManager().persist(dto);
    } catch (EntityExistsException e) {
      throw new DuplicateObjectException(DoosLayer.PERSISTENCE, dto,
                                         "create(T dto)");
    }
    getEntityManager().flush();
    getEntityManager().refresh(dto);

    return dto;
  }

  public void delete(T dto) {
    T merged  = getEntityManager().merge(dto);
    getEntityManager().remove(merged);
    getEntityManager().flush();

  }

  public List<T> getAll() {
    var builder = getEntityManager().getCriteriaBuilder();
    var query   = builder.createQuery(dto);
    var from    = query.from(dto);
    var all     = query.select(from);

    return query(all);
  }

  public List<T> getAll(DoosFilter<T> filter) {
    return getAll(filter, null);
  }

  public List<T> getAll(DoosFilter<T> filter, DoosSort<T> sort) {
    var builder = getEntityManager().getCriteriaBuilder();
    var query   = builder.createQuery(dto);
    var from    = query.from(dto);
    if (null != filter) {
      filter.execute(builder, from, query);
    }
    if (null != sort) {
      sort.execute(builder, from, query);
    }

    return query(query);
  }

  public List<T> getAll(DoosSort<T> sort) {
    return getAll(null, sort);
  }

  public T getByPrimaryKey(Object sleutel) {
    var entry = getEntityManager().find(dto, sleutel);

    if (null == entry) {
      throw new ObjectNotFoundException(DoosLayer.PERSISTENCE,
                                        "getByPrimaryKey("
                                          + sleutel.toString() + ")");
    }

    return entry;
  }

  public T getUniqueResult(DoosFilter<T> filter) {
    var builder   = getEntityManager().getCriteriaBuilder();
    var query     = builder.createQuery(dto);
    var from      = query.from(dto);
    filter.execute(builder, from, query);
    var resultaat = query(query);
    if (resultaat.isEmpty()) {
      throw new ObjectNotFoundException(DoosLayer.PERSISTENCE,
                                        "getUniqueResult(" + filter.toString()
                                          + ")");
    }
    if (resultaat.size() > 1) {
      throw new DuplicateObjectException(DoosLayer.PERSISTENCE, dto,
                                         "getUniqueResult(" + filter.toString()
                                           + ")");
    }

    return resultaat.get(0);
  }

  public Long namedNonSelect(String querynaam) {
    return namedNonSelect(querynaam, new HashMap<>());
  }

  public Long namedNonSelect(String querynaam,
                                   Map<String, Object> params) {
    var query = getEntityManager().createNamedQuery(querynaam);

    params.entrySet().forEach(entry ->
        query.setParameter(entry.getKey(), entry.getValue()));

    return Long.valueOf(query.executeUpdate());
  }

  public List<T> namedQuery(String querynaam) {
    return namedQuery(querynaam, new HashMap<>());
  }

  public List<T> namedQuery(String querynaam, Map<String, Object> params) {
    var query = getEntityManager().createNamedQuery(querynaam);

    params.entrySet().forEach(entry -> query.setParameter(entry.getKey(),
                                                          entry.getValue()));

    var resultaat = query.getResultList();

    if (null == resultaat) {
      resultaat = new ArrayList<>();
    }

    return resultaat;
  }

  public Object namedSingleResult(String querynaam) {
    return namedSingleResult(querynaam, new HashMap<>());
  }

  public Object namedSingleResult(String querynaam,
                                  Map<String, Object> params) {
    var query = getEntityManager().createNamedQuery(querynaam);

    params.entrySet().forEach(entry ->
        query.setParameter(entry.getKey(), entry.getValue()));

    return query.getSingleResult();
  }

  public List<T> query(CriteriaQuery<T> query) {
    var resultaat = getEntityManager().createQuery(query).getResultList();

    if (null == resultaat) {
      resultaat = new ArrayList<>();
    }

    return resultaat;
  }

  public T update(T dto) {
    var updated = getEntityManager().merge(dto);
    getEntityManager().persist(updated);
    getEntityManager().flush();
    getEntityManager().refresh(updated);

    return updated;
  }
}
