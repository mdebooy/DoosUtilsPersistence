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
package eu.debooy.doosutils.access;

import eu.debooy.doosutils.domain.DoosFilter;
import eu.debooy.doosutils.domain.DoosSort;
import eu.debooy.doosutils.domain.Dto;
import eu.debooy.doosutils.errorhandling.exception.DuplicateObjectException;
import eu.debooy.doosutils.errorhandling.exception.base.DoosLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.openjpa.util.ObjectNotFoundException;


/**
 * Data Access Object pattern.
 * 
 * @author Marco de Booij
 */
public abstract class Dao<T extends Dto> {
  protected abstract  EntityManager getEntityManager();

  private Class<T>  dto;

  public Dao(Class<T> dto) {
    this.dto  = dto;
  }

  public T create(T dto) {
    if (getEntityManager().contains(dto)) {
      throw new DuplicateObjectException(DoosLayer.PERSISTENCE, dto,
                                         "create(T dto)");
    }

    getEntityManager().persist(dto);
    getEntityManager().flush();
    getEntityManager().refresh(dto);

    return dto;
  }

  public void delete(T dto) {
    T merged  = (T) getEntityManager().merge(dto);
    getEntityManager().remove(merged);
    getEntityManager().flush();

  }

  public List<T> getAll() {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(dto);
    Root<T>           from      = query.from(dto);
    CriteriaQuery<T>  all       = query.select(from);

    return query(all);
  }

  public List<T> getAll(DoosFilter<T> filter) {
    return getAll(filter, null);
  }

  public List<T> getAll(DoosFilter<T> filter, DoosSort<T> sort) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(dto);
    Root<T>           from      = query.from(dto);
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

  public T getByPrimaryKey(Object primaryKey) {
    return getEntityManager().find(dto, primaryKey);
  }

  public T getUniqueResult(DoosFilter<T> filter) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(dto);
    Root<T>           from      = query.from(dto);
    filter.execute(builder, from, query);
    List<T>           resultaat = query(query);
    if (resultaat.isEmpty()) {
      throw new ObjectNotFoundException("getUniqueResult(" + filter.toString()
                                        + ")");
    }
    if (resultaat.size() > 1) {
      throw new DuplicateObjectException(DoosLayer.PERSISTENCE, dto,
                                         "getUniqueResult(" + filter.toString()
                                         + ")");
    }

    return resultaat.get(0);
  }

  public List<T> namedQuery(String querynaam) {
    return namedQuery(querynaam, new HashMap<String, Object>());
  }

  @SuppressWarnings("unchecked")
  public List<T> namedQuery(String querynaam, Map<String, Object> params) {
    Query query = getEntityManager().createNamedQuery(querynaam);

    for (Entry<String, Object> entry : params.entrySet()) {
      query.setParameter(entry.getKey(), entry.getValue());
    }

    List<T> resultaat = query.getResultList();

    if (null == resultaat) {
      resultaat = new ArrayList<T>();
    }
    
    return resultaat;
  }

  public List<T> query(CriteriaQuery<T> query) {
    List<T> resultaat = getEntityManager().createQuery(query).getResultList();

    if (null == resultaat) {
      resultaat = new ArrayList<T>();
    }

    return resultaat;
  }

  public T update(T dto) {
    T updated = (T) getEntityManager().merge(dto);
    getEntityManager().persist(updated);
    getEntityManager().flush();
    getEntityManager().refresh(updated);

    return updated;
  }
}
