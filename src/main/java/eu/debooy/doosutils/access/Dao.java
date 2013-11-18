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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;


/**
 * Data Access Object pattern.
 * 
 * @author Marco de Booij
 */
@Interceptors({PersistenceExceptionHandlerInterceptor.class})
public abstract class Dao<T extends Dto> implements Serializable {
  private static final  long  serialVersionUID = 1L;

  protected abstract  EntityManager getEntityManager();

  private Class<T>  persistentClass;

  public Dao(Class<T> persistentClass) {
    this.persistentClass  = persistentClass;
  }

  /**
   * Converteer een List in een Set.
   * 
   * De Set is een TreeSet als de entities Comparable is. Anders een HashSet.
   * 
   * 
   * @param List<T> Een lijst met DTOs
   * @return Collection<T>
   */
  public Collection<T> convertToCollection(List<T> entities) {
    if (null == entities || entities.size() == 0) {
      return null;
    }

    if (entities.get(0) instanceof Comparable<?>) {
      return new TreeSet<T>(entities);
    } else {
      return entities;
    }
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
   * @return Collection<T>
   */
  public Collection<T> getAll() {
    String        findAll = "select OBJECT(rij) from "
                            + persistentClass.getSimpleName()
                            + " AS rij";
    TypedQuery<T> query   =
        getEntityManager().createQuery(findAll, persistentClass);

    return convertToCollection(query.getResultList());
  }

  /**
   * Haal alle rijen op uit de database die aan de filter voldoen.
   * 
   * @param Een filter
   * @return Collection<T>
   */
  public Collection<T> getAll(DoosFilter<T> filter) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(persistentClass);
    Root<T>           from      = query.from(persistentClass);
    filter.execute(builder, from, query);

    return convertToCollection(getEntityManager().createQuery(query)
                                                 .getResultList());
  }

  /**
   * Haal alle rijen op uit de database die aan de filter voldoen gesorteerd op
   * sort.
   * 
   * @param DoosFilter<T> Een filter
   * @param DoosSort<T> Sorteer parameters
   * @return Collection<T>
   */
  public Collection<T> getAll(DoosFilter<T> filter, DoosSort<T> sort) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(persistentClass);
    Root<T>           from      = query.from(persistentClass);
    filter.execute(builder, from, query);
    sort.execute(builder, from, query);

    return convertToCollection(getEntityManager().createQuery(query)
                                                 .getResultList());
  }

  /**
   * Haal alle rijen op gesorteerd op sort.
   * 
   * @param DoosSort<T> Sorteer parameters
   * 
   * @return Collection<T>
   */
  public Collection<T> getAll(DoosSort<T> sort) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(persistentClass);
    Root<T>           from      = query.from(persistentClass);
    sort.execute(builder, from, query);

    return convertToCollection(getEntityManager().createQuery(query)
                                                 .getResultList());
  }

  public abstract String  getApplicatieNaam();

  /**
   * Krijg de entiteit via de Primary Key
   *
   * @param Object primaryKey
   * @return T
   */
  public T getByPrimaryKey(Object primaryKey) {
    return getEntityManager().find(persistentClass, primaryKey);
  }

  /**
   * Geef de logger.
   * 
   * @return Logger
   */
  public abstract Logger  getLogger();

  /**
   * Geef de dto die behoort bij de gevraagde filter.
   * 
   * @param DoosFilter<T> Een filter
   * @return T
   */
  public T getUniqueResult(DoosFilter<T> filter) {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(persistentClass);
    Root<T>           from      = query.from(persistentClass);
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
