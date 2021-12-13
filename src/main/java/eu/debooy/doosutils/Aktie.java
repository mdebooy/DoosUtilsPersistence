/**
 * Copyright 2016 Marco de Booij
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
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
package eu.debooy.doosutils;

import java.io.Serializable;


/**
 * @author Marco de Booij
 */
public class Aktie implements Serializable {
  private static final long serialVersionUID = 1L;

  private char  actie = PersistenceConstants.RETRIEVE;

  public Aktie() {}

  public Aktie(char aktie) {
    actie = aktie;
  }

  public Aktie(Aktie aktie) {
    actie = aktie.getAktie();
  }

  public char getAktie() {
    return actie;
  }

  public boolean isBekijk() {
    return (actie == PersistenceConstants.RETRIEVE);
  }

  public boolean isNieuw() {
    return (actie == PersistenceConstants.CREATE);
  }

  public boolean isReadonly() {
    return (actie == PersistenceConstants.DELETE)
        || (actie == PersistenceConstants.RETRIEVE);
  }

  public boolean isVerwijder() {
    return (actie == PersistenceConstants.DELETE);
  }

  public boolean isWijzig() {
    return (actie == PersistenceConstants.UPDATE);
  }

  public void setAktie(char aktie) {
    this.actie = aktie;
  }

  @Override
  public String toString() {
    return "Aktie: " + actie;
  }
}
