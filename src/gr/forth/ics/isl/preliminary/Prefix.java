/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.forth.ics.isl.preliminary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Prefix {
	public int id;
	int uris;
	int numberOfURIs;
	String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	Set<Integer> idsSet=new HashSet<Integer>();
	public int getNumberOfURIs() {
		return numberOfURIs;
	}
	public void setNumberOfURIs(int numberOfURIs) {
		this.numberOfURIs = numberOfURIs;
	}
	public int getUris() {
		return uris;
	}
	public void setUris(int uris) {
		this.uris = uris;
	}
	public String getIds() {
		return ids;
	}
	public void setIds(String ids) {
		this.ids = ids;
		String[] split=ids.split(",");
		this.setNumberOfURIs(split.length);
	}
	String ids;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void incrementURIs(){
		this.uris = uris+1;
	}
	public void addID(int did){
		idsSet.add(did);
	}
	public String docIDs(){
		String idsret="";
		for(int x:idsSet){
			idsret=x+",";
		}
		idsret=idsret.substring(0,idsret.length()-1);
		return idsret;
	}
}
