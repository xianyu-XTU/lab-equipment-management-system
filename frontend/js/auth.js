/**
 * 实验室设备管理系统 - 统一认证与页面跳转
 */
const LOGIN_PAGE = 'sign up and  sign in v2.html';
const ROLE_PAGES = {
    '管理员': 'board_admin.html',
    '实验员': 'board_lab.html',
    '学生': 'board_student.html'
};

function getLoggedUser() {
    const data = sessionStorage.getItem('lab_logged_user');
    return data ? JSON.parse(data) : null;
}

function setLoggedUser(user) {
    sessionStorage.setItem('lab_logged_user', JSON.stringify(user));
}

function requireAuth(allowedRoles) {
    const user = getLoggedUser();
    if (!user || !user.token) {
        window.location.href = LOGIN_PAGE;
        return null;
    }
    if (allowedRoles && !allowedRoles.includes(user.role)) {
        alert('您没有权限访问此页面');
        window.location.href = ROLE_PAGES[user.role] || LOGIN_PAGE;
        return null;
    }
    return user;
}

function logout() {
    sessionStorage.removeItem('lab_logged_user');
    window.location.href = LOGIN_PAGE;
}

function redirectIfLoggedIn() {
    const user = getLoggedUser();
    if (user && user.token && ROLE_PAGES[user.role]) {
        window.location.href = ROLE_PAGES[user.role];
    }
}

function getRolePage(role) {
    return ROLE_PAGES[role] || LOGIN_PAGE;
}
