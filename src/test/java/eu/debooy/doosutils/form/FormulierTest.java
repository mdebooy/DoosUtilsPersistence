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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;


/**
 * @author Marco de Booij
 */
public class FormulierTest {
  @Test
  public void testInit1() {
    var formulier = new Formulier();

    assertNull(formulier.getLogger());
  }

  @Test
  public void testInit2() {
    var formulier = new Testformulier();

    assertNull(formulier.getFormulier());
    assertNull(formulier.getOpmerking());
    assertFalse(formulier.isAktief());
    assertNull(formulier.getLogger());
  }

  @Test
  public void testToString1() {
    var formulier = new Formulier();

    assertEquals("Formulier (logger=<null>, "
                  + "class=[class eu.debooy.doosutils.form.Formulier])",
                 formulier.toString());
  }

  @Test
  public void testToString2() {
    var formulier = new Testformulier();

    assertEquals("Testformulier (formulier=<null>, opmerking=<null>, "
                  + "aktief=[false], logger=<null>, "
                  + "class=[class eu.debooy.doosutils.form.Testformulier])",
                 formulier.toString());

    var formulier2  = new Testformulier();
    formulier.setFormulier(formulier2);
    formulier.setOpmerking("opmerking");

    assertEquals("Testformulier (formulier=<Testformulier>, "
                  + "opmerking=[opmerking], aktief=[false], logger=<null>, "
                  + "class=[class eu.debooy.doosutils.form.Testformulier])",
                 formulier.toString());
  }
}
