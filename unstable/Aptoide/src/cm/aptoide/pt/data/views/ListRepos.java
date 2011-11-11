/*
 * ListRepos		part of Aptoide's data model
 * Copyright (C) 2011  Duarte Silveira
 * duarte.silveira@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package cm.aptoide.pt.data.views;

import java.util.LinkedHashMap;

 /**
 * ListRepos, models a list of Repos,
 * 			 maintains insertion order
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ListRepos {

	private LinkedHashMap<Integer, Repository> reposList;

	
	/**
	 * ListRepos Constructor
	 * 
	 * @param Repository repo
	 */
	public ListRepos(Repository repo) {
		this();
		addRepo(repo);
	}
	
	public ListRepos() {
		this.reposList = new LinkedHashMap<Integer, Repository>(1);
	}
	
	
	public void addRepo(Repository repo){
		this.reposList.put(repo.getHashid(), repo);
	}
	
	public void removeRepo(int repoHashid){
		this.reposList.remove(repoHashid);		
	}
	
	public void removeRepo(Repository repo){
		this.reposList.remove(repo.getHashid());		
	}
	
	public Repository getRepo(int repoHashid){
		return this.reposList.get(repoHashid);
	}
	
	
	/**
	 * getList, retrieves repos list,
	 * 			maintains insertion order
	 * 
	 * @return LinkedHashMap<Integer, Repository> reposList
	 */
	public LinkedHashMap<Integer, Repository> getList(){
		return this.reposList;
	}

	
	
	public void clean(){
		this.reposList = null;
	}
	
	public void reuse(Repository repo) {
		this.reposList = new LinkedHashMap<Integer, Repository>(1);
		addRepo(repo);
	}
	
}
