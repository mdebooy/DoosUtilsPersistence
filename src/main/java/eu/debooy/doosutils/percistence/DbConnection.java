/*
 * Copyright (c) 2022 Marco de Booij
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.debooy.doosutils.percistence;

import eu.debooy.doosutils.DoosUtils;
import eu.debooy.doosutils.PersistenceConstants;
import eu.debooy.doosutils.PersistenceUtils;
import eu.debooy.doosutils.errorhandling.exception.base.DoosError;
import eu.debooy.doosutils.errorhandling.exception.base.DoosException;
import eu.debooy.doosutils.errorhandling.exception.base.DoosLayer;
import java.text.MessageFormat;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;


/**
 * @author Marco de Booij
 */
public class DbConnection implements AutoCloseable {
  private EntityManager em  = null;

  private final String  dburl;
  private final String  dbuser;
  private final String  dbvendor;
  private final String  persistenceUnitName;
  private       String  wachtwoord;

  private DbConnection(DbConnection.Builder builder) {
    dburl               = builder.getDbUrl();
    dbuser              = builder.getDbUser();
    dbvendor            = builder.getDbVendor();
    persistenceUnitName = builder.getPersistenceUnitName();
    wachtwoord          = builder.getWachtwoord();
  }

  public static final class Builder {
    private String  dburl;
    private String  dbuser;
    private String  dbvendor            = "jdbc:postgresql://";
    private String  persistenceUnitName;
    private String  wachtwoord          = null;

    public DbConnection build() {
      return new DbConnection(this);
    }

    public String getDbUrl() {
      return dburl;
    }

    public String getDbUser() {
      return dbuser;
    }

    public String getDbVendor() {
      return dbvendor;
    }

    public String getPersistenceUnitName() {
      return persistenceUnitName;
    }

    public String getWachtwoord() {
      return wachtwoord;
    }

    public Builder setDbUrl(String dburl) {
      this.dburl                = dburl;
      return this;
    }

    public Builder setDbUser(String dbuser) {
      this.dbuser               = dbuser;
      return this;
    }

    public Builder setDbVendor(String dbvendor) {
      this.dbvendor             = dbvendor;
      return this;
    }

    public Builder setPersistenceUnitName(String persistenceUnitName) {
      this.persistenceUnitName  = persistenceUnitName;
      return this;
    }

    public Builder setWachtwoord(String wachtwoord) {
      this.wachtwoord           = wachtwoord;
      return this;
    }
  }

  @Override
  public void close() throws Exception {
    if (em.isOpen()) {
      em.close();
    }
  }

  public EntityManager getEntityManager() throws DoosException {
    var props = new Properties();
    if (null == wachtwoord) {
      wachtwoord  =
        DoosUtils.getWachtwoord(MessageFormat.format(
            PersistenceUtils.getMessage(PersistenceConstants.LBL_WACHTWOORD),
            dbuser, dburl.split("/")[1]));
    }

    props.put("openjpa.ConnectionURL",      dbvendor + dburl);
    props.put("openjpa.ConnectionUserName", dbuser);
    props.put("openjpa.ConnectionPassword", wachtwoord);

    try {
      em  = Persistence.createEntityManagerFactory(persistenceUnitName, props)
                       .createEntityManager();
    } catch (PersistenceException e) {
      throw new DoosException(DoosError.ILLEGAL_ARGUMENT,
                              DoosLayer.PERSISTENCE,
                              PersistenceUtils.getMessage(
              PersistenceConstants.ERR_INVALID_CONNECTION));
    }

    return em;
  }
}
