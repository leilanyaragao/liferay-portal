/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.analytics.settings.rest.internal.util.comparator;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;

/**
 * @author Thiago Buarque
 */
public class IgnoreCaseOrderByComparator<T> extends OrderByComparator<T> {

	public IgnoreCaseOrderByComparator(String tableName, Object... columns) {
		if ((columns.length == 0) || ((columns.length % 2) != 0)) {
			throw new IllegalArgumentException(
				"Columns length is not an even number");
		}

		_tableName = tableName;
		_columns = columns;
	}

	@Override
	public int compare(T object1, T object2) {
		for (int i = 0; i < _columns.length; i += 2) {
			String columnName = String.valueOf(_columns[i]);

			Object columnValue1 = BeanPropertiesUtil.getObjectSilent(
				object1, columnName);
			Object columnValue2 = BeanPropertiesUtil.getObjectSilent(
				object2, columnName);

			int value;

			if ((columnValue1 instanceof String) &&
				(columnValue2 instanceof String)) {

				String columnValue1String = (String)columnValue1;
				String columnValue2String = (String)columnValue2;

				if (Validator.isXml(columnValue1String)) {
					Locale defaultLocale = LocaleUtil.getDefault();

					columnValue1String = LocalizationUtil.getLocalization(
						columnValue1String, defaultLocale.getLanguage());
					columnValue2String = LocalizationUtil.getLocalization(
						columnValue2String, defaultLocale.getLanguage());
				}

				value = columnValue1String.compareToIgnoreCase(
					columnValue2String);
			}
			else {
				Comparable<Object> columnValueComparable1 =
					(Comparable<Object>)columnValue1;
				Comparable<Object> columnValueComparable2 =
					(Comparable<Object>)columnValue2;

				value = columnValueComparable1.compareTo(
					columnValueComparable2);
			}

			if (value == 0) {
				continue;
			}

			boolean columnAscending = Boolean.valueOf(
				String.valueOf(_columns[i + 1]));

			if (columnAscending) {
				return value;
			}

			return -value;
		}

		return 0;
	}

	@Override
	public String getOrderBy() {
		StringBundler sb = new StringBundler((5 * _columns.length) - 1);

		for (int i = 0; i < _columns.length; i += 2) {
			if (i != 0) {
				sb.append(StringPool.COMMA);
			}

			sb.append(_tableName);
			sb.append(StringPool.PERIOD);

			String columnName = String.valueOf(_columns[i]);
			boolean columnAscending = Boolean.valueOf(
				String.valueOf(_columns[i + 1]));

			sb.append(columnName);

			if (columnAscending) {
				sb.append(_ORDER_BY_ASC);
			}
			else {
				sb.append(_ORDER_BY_DESC);
			}
		}

		return sb.toString();
	}

	@Override
	public boolean isAscending(String field) {
		String orderBy = getOrderBy();

		if (orderBy == null) {
			return false;
		}

		int x = orderBy.indexOf(StringPool.PERIOD + field + StringPool.SPACE);

		if (x == -1) {
			return false;
		}

		int y = orderBy.indexOf(_ORDER_BY_ASC, x);

		if (y == -1) {
			return false;
		}

		int z = orderBy.indexOf(_ORDER_BY_DESC, x);

		if ((z >= 0) && (z < y)) {
			return false;
		}

		return true;
	}

	private static final String _ORDER_BY_ASC = " ASC";

	private static final String _ORDER_BY_DESC = " DESC";

	private final Object[] _columns;
	private final String _tableName;

}