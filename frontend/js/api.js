/**
 * 实验室设备管理系统 - 后端 API 统一封装
 */
const API_BASE = 'http://localhost:8080';

const ROLE_ID_MAP = { '学生': 1, '实验员': 2, '管理员': 3 };
const ROLE_CODE_MAP = { ADMIN: '管理员', LAB_ADMIN: '实验员', STUDENT: '学生' };
const ROLE_NAME_TO_CODE = { '管理员': 'ADMIN', '实验员': 'LAB_ADMIN', '学生': 'STUDENT' };

const DEVICE_STATUS_TEXT = { 1: '可借', 2: '已借出', 3: '维修中', 4: '报废' };
const DEVICE_STATUS_CODE = { '可借': 1, '已借出': 2, '维修中': 3, '报废': 4 };

const APPLY_STATUS_TEXT = { 0: '待审批', 1: '已通过', 2: '已拒绝' };
const RECORD_STATUS_TEXT = { 1: '借用中', 2: '已归还', 3: '逾期' };
const REPAIR_STATUS_TEXT = { 0: '待处理', 1: '维修中', 2: '已完成' };
const REPAIR_STATUS_CODE = { '待处理': 0, '维修中': 1, '已完成': 2 };

let _categories = [];
let _userMap = {};
let _deviceMap = {};

function getToken() {
    const user = getLoggedUser();
    return user ? user.token : null;
}

async function request(method, path, body, auth = true) {
    const headers = { 'Content-Type': 'application/json' };
    if (auth) {
        const token = getToken();
        if (token) headers['Authorization'] = 'Bearer ' + token;
    }
    const opts = { method, headers };
    if (body !== undefined) opts.body = JSON.stringify(body);

    let res;
    try {
        res = await fetch(API_BASE + path, opts);
    } catch (e) {
        throw new Error('无法连接后端服务，请确认后端已启动（' + API_BASE + '）');
    }

    let json;
    try {
        json = await res.json();
    } catch (e) {
        throw new Error('服务器响应格式错误');
    }

    if (res.status === 401) {
        sessionStorage.removeItem('lab_logged_user');
        window.location.href = 'sign up and  sign in v2.html';
        throw new Error('登录已过期，请重新登录');
    }

    if (json.code !== 200) {
        throw new Error(json.message || '操作失败');
    }
    return json.data;
}

async function fetchAllPages(fetchPage) {
    const all = [];
    let page = 1;
    const size = 100;
    while (true) {
        const data = await fetchPage(page, size);
        const records = data.records || [];
        all.push(...records);
        if (records.length < size || page >= (data.pages || 1)) break;
        page++;
    }
    return all;
}

function formatDateTime(val) {
    if (!val) return '-';
    return String(val).replace('T', ' ').substring(0, 16);
}

function roleIdToName(roleId) {
    const map = { 1: '学生', 2: '实验员', 3: '管理员' };
    return map[roleId] || '未知';
}

function roleNameToId(name) {
    return ROLE_ID_MAP[name] || 1;
}

function deviceStatusText(code) {
    return DEVICE_STATUS_TEXT[code] || '未知';
}

function deviceStatusCode(text) {
    return DEVICE_STATUS_CODE[text] ?? 1;
}

function repairStatusText(code) {
    return REPAIR_STATUS_TEXT[code] ?? '未知';
}

function repairStatusCode(text) {
    return REPAIR_STATUS_CODE[text] ?? 0;
}

function applyStatusText(code) {
    return APPLY_STATUS_TEXT[code] ?? '未知';
}

function recordStatusText(code) {
    return RECORD_STATUS_TEXT[code] ?? '未知';
}

async function loadCategories() {
    _categories = await request('GET', '/api/categories/all');
    return _categories;
}

function getCategoryName(categoryId) {
    const cat = _categories.find(c => c.id === categoryId);
    return cat ? cat.categoryName : '-';
}

function findCategoryIdByName(name) {
    if (!name) return null;
    const cat = _categories.find(c => c.categoryName === name.trim());
    return cat ? cat.id : null;
}

async function refreshUserMap() {
    const users = await fetchAllPages((p, s) => request('GET', `/api/users?page=${p}&size=${s}`));
    _userMap = {};
    users.forEach(u => { _userMap[u.id] = u; });
    return _userMap;
}

async function refreshDeviceMap() {
    const devices = await fetchAllPages((p, s) => request('GET', `/api/devices?page=${p}&size=${s}`));
    _deviceMap = {};
    devices.forEach(d => { _deviceMap[d.id] = d; });
    return _deviceMap;
}

function getUserName(userId) {
    const u = _userMap[userId];
    return u ? (u.realName || u.username) : ('用户#' + userId);
}

function getDeviceName(deviceId) {
    const d = _deviceMap[deviceId];
    return d ? d.deviceName : ('设备#' + deviceId);
}

function mapDevice(d) {
    return {
        id: d.id,
        device_no: d.deviceNo,
        device_name: d.deviceName,
        category: getCategoryName(d.categoryId),
        categoryId: d.categoryId,
        location: d.location || '-',
        status: deviceStatusText(d.status),
        statusCode: d.status,
        model: d.model,
        description: d.description
    };
}

function mapUser(u) {
    return {
        id: u.id,
        username: u.username,
        realName: u.realName,
        phone: u.phone,
        email: u.email,
        role: roleIdToName(u.roleId),
        roleId: u.roleId,
        roleCode: ROLE_NAME_TO_CODE[roleIdToName(u.roleId)] || 'STUDENT',
        status: u.status ?? 1
    };
}

function mapApply(a) {
    return {
        id: a.id,
        user_id: a.userId,
        user_name: getUserName(a.userId),
        device_id: a.deviceId,
        device_name: getDeviceName(a.deviceId),
        reason: a.applyReason,
        apply_time: formatDateTime(a.applyTime),
        expected_return: formatDateTime(a.expectedReturnTime),
        expectedReturnTime: a.expectedReturnTime,
        status: applyStatusText(a.status),
        statusCode: a.status,
        approve_remark: a.approveRemark
    };
}

function mapRecord(r) {
    const apply = r._apply;
    return {
        id: r.id,
        apply_id: r.applyId,
        user_id: r.userId,
        user_name: getUserName(r.userId),
        device_id: r.deviceId,
        device_name: getDeviceName(r.deviceId),
        borrow_time: formatDateTime(r.borrowTime),
        return_time: formatDateTime(r.returnTime),
        expected_return: apply ? formatDateTime(apply.expectedReturnTime) : '-',
        status: recordStatusText(r.status),
        statusCode: r.status,
        remark: r.remark
    };
}

function mapRepair(r) {
    return {
        id: r.id,
        user_id: r.userId,
        user_name: getUserName(r.userId),
        device_id: r.deviceId,
        device_name: getDeviceName(r.deviceId),
        fault_desc: r.faultDesc,
        report_time: formatDateTime(r.reportTime),
        status: repairStatusText(r.repairStatus),
        statusCode: r.repairStatus,
        result: r.repairResult || '-'
    };
}

function mapNotice(n) {
    return {
        id: n.id,
        title: n.title,
        content: n.content,
        publish_time: formatDateTime(n.publishTime),
        publisher: getUserName(n.publishUserId),
        status: n.status
    };
}

const Api = {
    async login(username, password) {
        const data = await request('POST', '/api/auth/login', { username, password }, false);
        return {
            token: data.token,
            id: data.userId,
            username: data.username,
            realName: data.realName,
            roleCode: data.roleCode,
            role: ROLE_CODE_MAP[data.roleCode] || '学生'
        };
    },

    async register({ username, password, realName, phone, email }) {
        await request('POST', '/api/auth/register', { username, password, realName, phone, email }, false);
    },

    async getProfile() {
        const u = await request('GET', '/api/profile/me');
        return mapUser(u);
    },

    async getStats() {
        return request('GET', '/api/stats/overview');
    },

    async getUsers(keyword) {
        await refreshUserMap();
        let users = Object.values(_userMap).map(mapUser);
        if (keyword) {
            users = users.filter(u => u.username.includes(keyword) || (u.realName && u.realName.includes(keyword)));
        }
        return users;
    },

    async saveUser(userData, editingId) {
        const payload = {
            username: userData.username,
            realName: userData.realName,
            phone: userData.phone,
            roleId: roleNameToId(userData.role),
            status: userData.status
        };
        if (editingId) {
            await request('PUT', `/api/users/${editingId}`, payload);
        } else {
            payload.password = '123456';
            await request('POST', '/api/users', payload);
        }
    },

    async resetUserPassword(userId) {
        await request('PUT', `/api/users/${userId}`, { password: '123456' });
    },

    async toggleUserStatus(userId, currentStatus) {
        await request('PUT', `/api/users/${userId}`, { status: currentStatus === 1 ? 0 : 1 });
    },

    async getDevices(keyword) {
        await loadCategories();
        const path = keyword
            ? `/api/devices?page=1&size=500&keyword=${encodeURIComponent(keyword)}`
            : '/api/devices?page=1&size=500';
        const data = await request('GET', path);
        const devices = (data.records || []).map(mapDevice);
        devices.forEach(d => { _deviceMap[d.id] = { id: d.id, deviceName: d.device_name, deviceNo: d.device_no }; });
        return devices;
    },

    async saveDevice(deviceData, editingId) {
        await loadCategories();
        const payload = {
            deviceNo: deviceData.device_no,
            deviceName: deviceData.device_name,
            categoryId: findCategoryIdByName(deviceData.category),
            location: deviceData.location,
            status: deviceStatusCode(deviceData.status)
        };
        if (editingId) {
            await request('PUT', `/api/devices/${editingId}`, payload);
        } else {
            await request('POST', '/api/devices', payload);
        }
    },

    async deleteDevice(id) {
        await request('DELETE', `/api/devices/${id}`);
    },

    async getApplies(status) {
        await refreshUserMap();
        await refreshDeviceMap();
        let path = '/api/borrow/applies?page=1&size=500';
        if (status !== undefined && status !== null) path += `&status=${status}`;
        const data = await request('GET', path);
        return (data.records || []).map(mapApply);
    },

    async getRecords(status) {
        await refreshUserMap();
        await refreshDeviceMap();
        let path = '/api/borrow/records?page=1&size=500';
        if (status !== undefined && status !== null) path += `&status=${status}`;
        const data = await request('GET', path);
        const rawApplies = await fetchAllPages((p, s) => request('GET', `/api/borrow/applies?page=${p}&size=${s}`));
        const applyMap = {};
        rawApplies.forEach(a => { applyMap[a.id] = a; });
        return (data.records || []).map(r => {
            r._apply = applyMap[r.applyId] || null;
            return mapRecord(r);
        });
    },

    async approveBorrow(applyId, approved, remark) {
        await request('POST', '/api/borrow/approve', {
            applyId,
            status: approved ? 1 : 2,
            approveRemark: remark || ''
        });
    },

    async returnDevice(recordId, remark) {
        await request('POST', '/api/borrow/return', { recordId, remark: remark || '' });
    },

    async applyBorrow(deviceId, applyReason, expectedReturnTime) {
        await request('POST', '/api/borrow/apply', {
            deviceId,
            applyReason,
            expectedReturnTime
        });
    },

    async getRepairs(repairStatus) {
        await refreshUserMap();
        await refreshDeviceMap();
        let path = '/api/repairs?page=1&size=500';
        if (repairStatus !== undefined && repairStatus !== null) path += `&repairStatus=${repairStatus}`;
        const data = await request('GET', path);
        return (data.records || []).map(mapRepair);
    },

    async reportRepair(deviceId, faultDesc) {
        await request('POST', '/api/repairs/report', { deviceId, faultDesc });
    },

    async handleRepair(repairId, repairStatus, repairResult) {
        await request('POST', '/api/repairs/handle', { repairId, repairStatus, repairResult });
    },

    async deleteRepair(id) {
        await request('DELETE', `/api/repairs/${id}`);
    },

    async getNotices(keyword) {
        await refreshUserMap();
        let path = '/api/notices?page=1&size=500';
        if (keyword) path += `&keyword=${encodeURIComponent(keyword)}`;
        const data = await request('GET', path);
        return (data.records || []).map(mapNotice);
    },

    async publishNotice(title, content) {
        await request('POST', '/api/notices', { title, content, status: 1 });
    },

    async deleteNotice(id) {
        await request('DELETE', `/api/notices/${id}`);
    },

    async getMyBorrowApplies(status) {
        await refreshDeviceMap();
        let path = '/api/my/borrow-applies?page=1&size=500';
        if (status !== undefined && status !== null) path += `&status=${status}`;
        const data = await request('GET', path);
        return (data.records || []).map(a => {
            const m = mapApply(a);
            m.user_name = getLoggedUser().realName;
            return m;
        });
    },

    async getMyBorrowRecords(status) {
        await refreshDeviceMap();
        let path = '/api/my/borrow-records?page=1&size=500';
        if (status !== undefined && status !== null) path += `&status=${status}`;
        const data = await request('GET', path);
        const rawApplies = await fetchAllPages((p, s) => request('GET', `/api/my/borrow-applies?page=${p}&size=${s}`));
        const applyMap = {};
        rawApplies.forEach(a => { applyMap[a.id] = a; });
        return (data.records || []).map(r => {
            r._apply = applyMap[r.applyId] || null;
            const m = mapRecord(r);
            m.user_name = getLoggedUser().realName;
            return m;
        });
    },

    async getMyRepairs(repairStatus) {
        await refreshDeviceMap();
        let path = '/api/my/repairs?page=1&size=500';
        if (repairStatus !== undefined && repairStatus !== null) path += `&repairStatus=${repairStatus}`;
        const data = await request('GET', path);
        return (data.records || []).map(r => {
            const m = mapRepair(r);
            m.user_name = getLoggedUser().realName;
            return m;
        });
    },

    loadCategories,
    formatDateTime,
    deviceStatusText,
    deviceStatusCode,
    repairStatusText,
    repairStatusCode,
    applyStatusText,
    recordStatusText
};

async function handleApiError(err) {
    alert(err.message || '操作失败');
}
