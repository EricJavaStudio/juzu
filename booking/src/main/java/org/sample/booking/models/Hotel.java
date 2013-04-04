/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sample.booking.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
// @Entity
public class Hotel {

  private static final AtomicInteger sequence = new AtomicInteger();
  private static Map<String, Hotel> hotels = new LinkedHashMap<String, Hotel>();

  private static void add(Hotel hotel) {
    hotels.put(hotel.id, hotel);
  }

  static {
    add(new Hotel("Marriott Courtyard", "Tower Place, Buckhead", "Atlanta", "GA", "30305", "USA", new BigDecimal(120)));
    add(new Hotel("Doubletree", "Tower Place, Buckhead", "Atlanta", "GA", "30305", "USA", new BigDecimal(180)));
    add(new Hotel("Hotel Rouge", "1315 16th Street NW", "Washington", "DC", "20036", "USA", new BigDecimal(250)));
    add(new Hotel("70 Park Avenue Hotel", "70 Park Avenue", "NY", "NY", "NY", "USA", new BigDecimal(300)));
    add(new Hotel("Conrad Miami", "1395 Brickell Ave", "Miami", "FL", "33131", "USA", new BigDecimal(300)));
    add(new Hotel("Sea Horse Inn", "2106 N Clairemont Ave", "Eau Claire", "WI", "54703", "USA", new BigDecimal(80)));
    add(new Hotel("Super 8 Eau Claire Campus Area", "1151 W Macarthur Av", "Eau Claire", "WI", "54703", "USA", new BigDecimal(90)));
    add(new Hotel("Marriott Downtown", "55 Fourth Street", "San Francisco", "CA", "94103", "USA", new BigDecimal(160)));
    add(new Hotel("Hilton Diagonal Mar", "Passeig del Taulat 262-264", "Barcelona", "Catalunya", "08019", "Spain", new BigDecimal(200)));
    add(new Hotel("Hilton Tel Aviv", "Independence Park", "Tel Aviv", "", "63405", "Israel", new BigDecimal(210)));
    add(new Hotel("InterContinental Tokyo Bay", "Takeshiba Pier", "Tokyo", "", "105", "Japan", new BigDecimal(240)));
    add(new Hotel("Hotel Beaulac", "Esplanade Léopold-Robert 2", "Neuchatel", "", "2000", "Switzerland", new BigDecimal(130)));
    add(new Hotel("Conrad Treasury Place", "William & George Streets", "Brisbane", "QLD", "4001", "Australia", new BigDecimal(140)));
    add(new Hotel("Ritz Carlton", "1228 Sherbrooke St", "West Montreal", "Quebec", "H3G1H6", "Canada", new BigDecimal(230)));
    add(new Hotel("Ritz Carlton", "Peachtree Rd, Buckhead", "Atlanta", "GA", "30326", "Atlanta", new BigDecimal(460)));
    add(new Hotel("Swissotel", "68 Market Street", "Sydney", "NSW", "2000", "Australia", new BigDecimal(220)));
    add(new Hotel("Meliá White House", "Albany Street", "Regents Park London", "", "NW13UP", "Great Britain", new BigDecimal(250)));
    add(new Hotel("Hotel Allegro", "171 West Randolph Street", "Chicago", "IL", "60601", "USA", new BigDecimal(210)));
  }

  public final String id = "" + sequence.getAndIncrement();

  //    @Required
//    @MaxSize(50)
  public String name;

  //    @MaxSize(100)
  public String address;

  //    @Required
//    @MaxSize(40)
  public String city;

  //    @Required
//    @MaxSize(6)
//    @MinSize(2)
  public String state;

  //    @Required
//    @MaxSize(6)
//    @MinSize(5)
  public String zip;

  //    @Required
//    @MaxSize(40)
//    @MinSize(2)
  public String country;

  //     @Column(precision=6, scale=2)
  public BigDecimal price;

  private Hotel(String name, String address, String city, String state, String zip, String country, BigDecimal price) {
    this.name = name;
    this.address = address;
    this.city = city;
    this.state = state;
    this.zip = zip;
    this.country = country;
    this.price = price;
  }

  public String toString() {
    return "Hotel(" + name + "," + address + "," + city + "," + zip + ")";
  }

  public static Hotel findById(String id) {
    return hotels.get(id);
  }

  public static List<Hotel> find(Pattern namePattern, Pattern cityPattern, int size, int page) {
    int skip = size * page;
    List<Hotel> found = new ArrayList<Hotel>();
    for (Hotel hotel : hotels.values()) {
      if (namePattern.matcher(hotel.name).matches() || cityPattern.matcher(hotel.city).matches()) {
        if (skip-- > 0) {
          //
        }
        else if (size-- > 0) {
          found.add(hotel);
        }
        else {
          break;
        }
      }
    }
    return found;
  }
}