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
public class Aktie implements Cloneable, Serializable {
  private static final long serialVersionUID = 1L;

  private char  aktie = PersistenceConstants.RETRIEVE;

  public Aktie() {}
  
  public Aktie(char aktie) {
    this.aktie  = aktie;
  }

  /**
   * Clone de Aktie
   * 
   * @return Aktie
   * @throws CloneNotSupportedException
   */
  public Aktie clone() throws CloneNotSupportedException {
    Aktie clone = (Aktie) super.clone();

    return clone;
  }

  /**
   * @return de aktie
   */
  public char getAktie() {
    return aktie;
  }

  /**
   * In 'Bekijk' mode?
   * 
   * @return boolean
   */
  public boolean isBekijk() {
    return (aktie == PersistenceConstants.RETRIEVE);
  }

  /**
   * In 'Nieuw' mode?
   * 
   * @return boolean
   */
  public boolean isNieuw() {
    return (aktie == PersistenceConstants.CREATE);
  }

  /**
   * In read-only mode?
   * 
   * @return boolean
   */
  public boolean isReadonly() {
    return (aktie == PersistenceConstants.DELETE)
        || (aktie == PersistenceConstants.RETRIEVE);
  }

  /**
   * In 'Verwijder' mode?
   * 
   * @return boolean
   */
  public boolean isVerwijder() {
    return (aktie == PersistenceConstants.DELETE);
  }

  /**
   * In 'Wijzig' mode?
   * 
   * @return boolean
   */
  public boolean isWijzig() {
    return (aktie == PersistenceConstants.UPDATE);
  }

  /**
   * @param aktie de waarde van aktie
   */
  public void setAktie(char aktie) {
    this.aktie = aktie;
  }

  @Override
  public String toString() {
    return "Aktie: " + aktie;
  }
}
