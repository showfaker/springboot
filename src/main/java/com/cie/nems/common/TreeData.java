package com.cie.nems.common;

import java.util.List;

import com.cie.nems.common.service.CommonService;

public class TreeData<E> {

	/**
	 * 当前节点对象
	 */
	private E data;

	/**
	 * 子节点列表
	 */
	private List<TreeData<E>> children;

	/**
	 * 是否为叶子节点
	 */
	private Boolean leaf;

	/**
	 * 是否展开子节点
	 */
	private Boolean expanded;
	
	/**
	 * 是否选中
	 */
	private Boolean selected;

	public E getData() {
		return data;
	}

	public void setData(E data) {
		this.data = data;
	}

	public List<TreeData<E>> getChildren() {
		return children;
	}

	public void setChildren(List<TreeData<E>> children) {
		this.children = children;
	}

	public Boolean getLeaf() {
		return leaf;
	}

	public void setLeaf(Boolean leaf) {
		this.leaf = leaf;
	}

	public Boolean getExpanded() {
		return expanded;
	}

	public void setExpanded(Boolean expanded) {
		this.expanded = expanded;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}
