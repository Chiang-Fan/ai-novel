/**
 * 安全访问工具 - 防止undefined/null访问错误
 */

/**
 * 安全获取数组，如果不是数组则返回空数组
 * @param {*} value - 要检查的值
 * @returns {Array} - 安全的数组
 */
export function safeArray(value) {
  return Array.isArray(value) ? value : []
}

/**
 * 安全获取对象，如果为null/undefined则返回空对象
 * @param {*} value - 要检查的值
 * @returns {Object} - 安全的对象
 */
export function safeObject(value) {
  return value && typeof value === 'object' && !Array.isArray(value) ? value : {}
}

/**
 * 安全获取字符串，如果为null/undefined则返回空字符串
 * @param {*} value - 要检查的值
 * @returns {string} - 安全的字符串
 */
export function safeString(value) {
  return value != null ? String(value) : ''
}

/**
 * 安全获取数字，如果为null/undefined/NaN则返回默认值
 * @param {*} value - 要检查的值
 * @param {number} defaultValue - 默认值
 * @returns {number} - 安全的数字
 */
export function safeNumber(value, defaultValue = 0) {
  const num = Number(value)
  return isNaN(num) ? defaultValue : num
}

/**
 * 安全获取嵌套属性
 * @param {Object} obj - 对象
 * @param {string} path - 属性路径，如 'a.b.c'
 * @param {*} defaultValue - 默认值
 * @returns {*} - 属性值或默认值
 */
export function safeGet(obj, path, defaultValue = undefined) {
  if (!obj || !path) return defaultValue
  
  const keys = path.split('.')
  let result = obj
  
  for (const key of keys) {
    if (result == null) return defaultValue
    result = result[key]
  }
  
  return result !== undefined ? result : defaultValue
}

/**
 * 安全检查数组长度
 * @param {*} arr - 要检查的数组
 * @returns {number} - 数组长度，非数组返回0
 */
export function safeLength(arr) {
  return Array.isArray(arr) ? arr.length : 0
}

/**
 * 安全的map操作
 * @param {*} arr - 要map的数组
 * @param {Function} fn - map函数
 * @returns {Array} - map结果，非数组返回空数组
 */
export function safeMap(arr, fn) {
  return Array.isArray(arr) ? arr.map(fn) : []
}

/**
 * 安全的filter操作
 * @param {*} arr - 要filter的数组
 * @param {Function} fn - filter函数
 * @returns {Array} - filter结果，非数组返回空数组
 */
export function safeFilter(arr, fn) {
  return Array.isArray(arr) ? arr.filter(fn) : []
}

/**
 * 安全的find操作
 * @param {*} arr - 要find的数组
 * @param {Function} fn - find函数
 * @returns {*} - find结果，非数组返回undefined
 */
export function safeFind(arr, fn) {
  return Array.isArray(arr) ? arr.find(fn) : undefined
}

/**
 * 安全的JSON解析
 * @param {string} jsonStr - JSON字符串
 * @param {*} defaultValue - 解析失败时的默认值
 * @returns {*} - 解析结果或默认值
 */
export function safeJsonParse(jsonStr, defaultValue = null) {
  if (typeof jsonStr !== 'string') return defaultValue
  
  try {
    return JSON.parse(jsonStr)
  } catch (error) {
    console.warn('JSON解析失败:', error)
    return defaultValue
  }
}

export default {
  safeArray,
  safeObject,
  safeString,
  safeNumber,
  safeGet,
  safeLength,
  safeMap,
  safeFilter,
  safeFind,
  safeJsonParse
}
