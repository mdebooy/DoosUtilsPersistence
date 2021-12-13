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
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


/**
 * @author Marco de Booij
 */
public final class EntityManagerUtil {
  private EntityManagerUtil() {}

  private static        EntityManagerFactory        emf;
  public  static final  ThreadLocal<EntityManager>  ENTITYMANAGER =
      new ThreadLocal<>();

  public static EntityManagerFactory getEntityManagerFactory(
      String persistenceUnitName) {
    if (null == emf) {
      emf = Persistence.createEntityManagerFactory(persistenceUnitName);
    }
    return emf;
  }

  public static EntityManagerFactory getEntityManagerFactory(
      String persistenceUnitName, String configuratie) {
    if (null == emf) {
      var mappings  = EntityManagerFactory.class.getClassLoader()
                                          .getResourceAsStream(configuratie);
      var properties  = new Properties();
      try {
        properties.load(mappings);
      } catch (IOException e) {
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
    var em  = ENTITYMANAGER.get();

    if (null == em) {
      getEntityManagerFactory(persistenceUnitName);
      em  = emf.createEntityManager();
      ENTITYMANAGER.set(em);
    }
    return em;
  }

  public static void closeEntityManager() {
    var em  = ENTITYMANAGER.get();
    ENTITYMANAGER.remove();
    if (null != em) {
      em.close();
    }
    if (null == emf) {
      return;
    }
    emf.close();
  }
}
