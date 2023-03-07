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
package eu.debooy.doosutils.form;

import java.io.Serializable;


/**
 * @author Marco de Booij
 */
public class Testformulier extends Formulier implements Serializable {
  private boolean   aktief;
  private Formulier form;
  private String    opmerking;

  public Formulier  getFormulier() {
    return form;
  }

  public String getOpmerking() {
    return opmerking;
  }

  public boolean isAktief() {
    return aktief;
  }

  public void setAktief(boolean aktief) {
    this.aktief     = aktief;
  }

  public void setFormulier(Formulier form) {
    this.form       = form;
  }

  public void setOpmerking(String opmerking) {
    this.opmerking  = opmerking;
  }
}
