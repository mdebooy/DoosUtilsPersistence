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
import eu.debooy.doosutils.errorhandling.handler.interceptor.PersistenceExceptionHandlerInterceptor;

import java.util.List;

import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


/**
 * Data Access Object pattern.
 * 
 * @author Marco de Booij
 */
@Interceptors({PersistenceExceptionHandlerInterceptor.class})
public abstract class Dao<T extends Dto> {
  /**
   * Maakt het mogelijk om de Entity Manager in the extended class te definieren
   * en toch hier te gebruiken.
   *  
   * @return De Entity Manager
   */
  protected abstract  EntityManager getEntityManager();

  private Class<T>  dto;

  public Dao(Class<T> dto) {
    this.dto  = dto;
  }

  /**
   * Schrijf de DTO in de database.
   * 
   * @param T Een DTO.
   * @return T
   */
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

  /**
   * Verwijder de DTO uit de database.
   * 
   * @param T Een DTO
   */
  public void delete(T dto) {
    T merged  = (T) getEntityManager().merge(dto);
    getEntityManager().remove(merged);
    getEntityManager().flush();

  }

  /**
   * Haal alle rijen op uit de database.
   * 
   * @return List<T>
   */
  public List<T> getAll() {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(dto);
    Root<T>           from      = query.from(dto);
    CriteriaQuery<T>  all       = query.select(from);
    TypedQuery<T>     allQuery  = getEntityManager().createQuery(all);

    return allQuery.getResultList();
  }

  /**
   * Haal alle rijen op uit de database die aan de filter voldoen.
   * 
   * @param Een filter
   * @return List<T>
   */
  public List<T> getAll(DoosFilter<T> filter) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(dto);
    Root<T>           from      = query.from(dto);
    filter.execute(builder, from, query);

    return getEntityManager().createQuery(query).getResultList();
  }

  /**
   * Haal alle rijen op uit de database die aan de filter voldoen gesorteerd op
   * sort.
   * 
   * @param DoosFilter<T> Een filter
   * @param DoosSort<T> Sorteer parameters
   * @return List<T>
   */
  public List<T> getAll(DoosFilter<T> filter, DoosSort<T> sort) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(dto);
    Root<T>           from      = query.from(dto);
    filter.execute(builder, from, query);
    sort.execute(builder, from, query);

    return getEntityManager().createQuery(query).getResultList();
  }

  /**
   * Haal alle rijen op gesorteerd op sort.
   * 
   * @param DoosSort<T> Sorteer parameters
   * 
   * @return List<T>
   */
  public List<T> getAll(DoosSort<T> sort) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(dto);
    Root<T>           from      = query.from(dto);
    sort.execute(builder, from, query);

    return getEntityManager().createQuery(query).getResultList();
  }

  /**
   * Krijg de entiteit via de Primary Key
   *
   * @param Object primaryKey
   * @return T
   */
  public T getByPrimaryKey(Object primaryKey) {
    return getEntityManager().find(dto, primaryKey);
  }

  /**
   * Geef de dto die behoort bij de gevraagde filter.
   * 
   * @param DoosFilter<T> Een filter
   * @return T
   */
  public T getUniqueResult(DoosFilter<T> filter) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(dto);
    Root<T>           from      = query.from(dto);
    filter.execute(builder, from, query);
    T                 resultaat = getEntityManager().createQuery(query)
                                                    .getSingleResult();

    return resultaat;
  }

  /**
   * Wijzig de data in de database.
   *  
   * @param T Een DTO
   * @return T
   */
  public T update(T dto) {
    T updated = (T) getEntityManager().merge(dto);
    getEntityManager().persist(updated);
    getEntityManager().flush();
    getEntityManager().refresh(updated);

    return updated;
  }
}
