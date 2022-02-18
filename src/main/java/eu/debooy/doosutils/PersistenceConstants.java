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
package eu.debooy.doosutils;


/**
 * @author Marco de Booij
 */
public final class PersistenceConstants {
  private PersistenceConstants() {}

  public static final char  CREATE    = 'C';
  public static final char  RETRIEVE  = 'R';
  public static final char  UPDATE    = 'U';
  public static final char  DELETE    = 'D';
  public static final char  SEARCH    = 'S';

  public static final String  CREATED     = "info.create";
  public static final String  DELETED     = "info.delete";
  public static final String  DUPLICATE   = "errors.duplicate";
  public static final String  EMPTY       = "errors.empty";
  public static final String  FIXLENGTH   = "errors.fixlength";
  public static final String  FUTURE      = "errors.date.future";
  public static final String  INVALID     = "errors.invalid";
  public static final String  ISGROTER    = "errors.is.groter";
  public static final String  ISKLEINER   = "errors.is.kleiner";
  public static final String  MAXLENGTH   = "errors.maxlength";
  public static final String  NIETGROTER  = "errors.niet.groter";
  public static final String  NIETKLEINER = "errors.niet.kleiner";
  public static final String  NOROWS      = "info.norows";
  public static final String  NOTFOUND    = "errors.notfound";
  public static final String  RANGE       = "errors.range";
  public static final String  REQUIRED    = "errors.required";
  public static final String  SEARCHED    = "info.search";
  public static final String  UPDATED     = "info.update";
  public static final String  WRONGACTION = "errors.aktie.wrong";
  public static final String  WRONGDATE   = "errors.date";
  public static final String  WRONGVALUE  = "errors.value";

  public static final  String GET   = "get";
  public static final  String LIKE  = "%";
  public static final  String IS    = "is";

  public static final String  ERR_INVALID_CONNECTION  =
      "error.invalid.connection";

  public static final String  LBL_DBURL         = "label.dburl";
  public static final String  LBL_DBUSER        = "label.dbuser";
  public static final String  LBL_DBUSER_E      = "label.dbuser.extra";
  public static final String  LBL_WACHTWOORD    = "label.wachtwoord";
  public static final String  LBL_WACHTWOORD_E  = "label.wachtwoord.extra";
  public static final String  LBL_WACHTWOORD_E2 = "label.wachtwoord.extra2";

}
