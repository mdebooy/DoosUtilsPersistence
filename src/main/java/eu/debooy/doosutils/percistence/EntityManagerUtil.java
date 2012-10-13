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
package eu.debooy.doosutils.percistence;

import eu.debooy.doosutils.errorhandling.exception.IllegalArgumentException;
import eu.debooy.doosutils.errorhandling.exception.base.DoosLayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Marco de Booij
 */
public class EntityManagerUtil {
  private EntityManagerUtil() {}

  private static        EntityManagerFactory        emf;
  private static final  Logger                      logger        =
      LoggerFactory.getLogger(EntityManagerUtil.class);
  public  static final  ThreadLocal<EntityManager>  entitymanager =
      new ThreadLocal<EntityManager>();

  public static EntityManagerFactory getEntityManagerFactory(
      String persistenceUnitName) {
    if (emf == null) {
      emf = Persistence.createEntityManagerFactory(persistenceUnitName);
    }
    return emf;
  }

  public static EntityManagerFactory getEntityManagerFactory(
      String persistenceUnitName, String configuratie)
      throws IllegalArgumentException {
    if (emf == null) {
      InputStream mappings  = null;
      mappings =  EntityManagerFactory.class.getClassLoader()
                                      .getResourceAsStream(configuratie);
      Properties  properties  = new Properties();
      try {
        properties.load(mappings);
      } catch (IOException e) {
        logger.error("getEntityManagerFactory: " + e.getMessage());
        throw new IllegalArgumentException(DoosLayer.PERSISTENCE,
                                           "getEntityManagerFactory: "
                                           + e.getMessage());
      }
      emf = Persistence.createEntityManagerFactory(persistenceUnitName,
                                                   properties);
    }

    return emf;
  }

  public static EntityManager getEntityManager(String persistenceUnitName) {
    EntityManager em  = (EntityManager) entitymanager.get();

    if (em == null) {
      getEntityManagerFactory(persistenceUnitName);
      em  = emf.createEntityManager();
      entitymanager.set(em);
    }
    return em;
  }

  public static void closeEntityManager() {
    EntityManager em  = (EntityManager) entitymanager.get();
    entitymanager.set(null);
    if (em != null) {
      em.close();
    }
    if (emf == null) {
      return;
    }
    emf.close();
  }
}
