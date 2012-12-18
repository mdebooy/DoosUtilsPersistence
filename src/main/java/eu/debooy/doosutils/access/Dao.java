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
import eu.debooy.doosutils.errorhandling.exception.IllegalArgumentException;
import eu.debooy.doosutils.errorhandling.exception.ObjectNotFoundException;
import eu.debooy.doosutils.errorhandling.exception.base.DoosLayer;
import eu.debooy.doosutils.errorhandling.exception.base.DoosRuntimeException;
import eu.debooy.doosutils.errorhandling.handler.interceptor.PersistenceExceptionHandlerInterceptor;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
   * @param entities
   * @return een TreeSet of HashSet
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
   * Persist de DTO
   * 
   * @param dto
   * @return
   * @throws DoosRuntimeException
   * @throws DuplicateObjectException
   */
  public T create(T dto) throws DoosRuntimeException, DuplicateObjectException {
    if (getEntityManager().contains(dto)) {
        throw new DuplicateObjectException(DoosLayer.PERSISTENCE, dto,
                                           "bestaat reeds");
    }

    getEntityManager().persist(dto);
    getEntityManager().flush();
    getEntityManager().refresh(dto);

    return dto;
  }

  /**
   * Verwijder de DTO
   * 
   * @param dto
   * @throws DoosRuntimeException
   */
  public void delete(T dto) throws DoosRuntimeException {
    T merged  = (T) getEntityManager().merge(dto);
    getEntityManager().remove(merged);
    getEntityManager().flush();

  }

  /**
   * Haal alle rijen op.
   * 
   * @return
   * @throws DoosRuntimeException
   * @throws ObjectNotFoundException
   */
  public Collection<T> getAll()
      throws DoosRuntimeException, ObjectNotFoundException {
    String        findAll = "select OBJECT(rij) from "
                            + persistentClass.getSimpleName()
                            + " AS rij";
    TypedQuery<T> query   =
        getEntityManager().createQuery(findAll, persistentClass);

    return convertToCollection(query.getResultList());
  }

  /**
   * Haal alle rijen op die aan de filter voldoen.
   * 
   * @param filter
   * @return
   * @throws DoosRuntimeException
   * @throws IllegalArgumentException
   * @throws ObjectNotFoundException
   */
  public Collection<T> getAll(DoosFilter<T> filter)
      throws DoosRuntimeException, IllegalArgumentException,
             ObjectNotFoundException {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(persistentClass);
    Root<T>           from      = query.from(persistentClass);
    filter.execute(builder, from, query);

    return convertToCollection(getEntityManager().createQuery(query)
                                                 .getResultList());
  }

  /**
   * Haal alle rijen op die aan de filter voldoen gesorteerd op sort.
   * 
   * @param filter
   * @param sort
   * @return
   * @throws DoosRuntimeException
   * @throws IllegalArgumentException
   * @throws ObjectNotFoundException
   */
  public Collection<T> getAll(DoosFilter<T> filter, DoosSort<T> sort)
      throws DoosRuntimeException, IllegalArgumentException,
             ObjectNotFoundException {
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
   * @param sort
   * @return
   * @throws DoosRuntimeException
   * @throws ObjectNotFoundException
   */
  public Collection<T> getAll(DoosSort<T> sort)
      throws DoosRuntimeException, ObjectNotFoundException {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(persistentClass);
    Root<T>           from      = query.from(persistentClass);
    sort.execute(builder, from, query);

    return convertToCollection(getEntityManager().createQuery(query)
                                                 .getResultList());
  }

  public abstract String  getApplicationName();

  /**
   * Krijg de entiteit via de Primary Key
   * @param primaryKey
   * @return
   * @throws DoosRuntimeException
   * @throws ObjectNotFoundException
   */
  public T getByPrimaryKey(Object primaryKey)
      throws DoosRuntimeException, ObjectNotFoundException {
    return getEntityManager().find(persistentClass, primaryKey);
  }

  public abstract Logger  getLogger();

  public T getUniqueResult(DoosFilter<T> filter)
      throws DoosRuntimeException, IllegalArgumentException,
             ObjectNotFoundException {
    CriteriaBuilder   builder   = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<T>  query     = builder.createQuery(persistentClass);
    Root<T>           from      = query.from(persistentClass);
    filter.execute(builder, from, query);
    // TODO Waarom moet nu ineens de NoResultException opgebvangen worden?
    T                 resultaat = null;
    try {
      resultaat = getEntityManager().createQuery(query).getSingleResult();
    } catch (NoResultException e) {
      throw new ObjectNotFoundException(DoosLayer.PERSISTENCE,
                                        e.getMessage(), e);
    }

    return resultaat;
  }

  public T update(T dto) throws DoosRuntimeException, ObjectNotFoundException {
    T updated = (T) getEntityManager().merge(dto);
    getEntityManager().persist(updated);
    getEntityManager().flush();
    getEntityManager().refresh(updated);

    return updated;
  }
}
