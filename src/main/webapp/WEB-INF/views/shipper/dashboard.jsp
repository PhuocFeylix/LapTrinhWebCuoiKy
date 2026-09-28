<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Dashboard Shipper - UTEExpress</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><link href="/resources/css/style.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark navbar-ute"><div class="container"><a class="navbar-brand fw-bold" href="/">UTEExpress</a><span class="text-white">Khu vực Shipper</span></div></nav>
<main class="container py-4">
<div class="d-flex justify-content-between align-items-center mb-4"><div><h2 class="fw-bold mb-1">Đơn giao của tôi</h2><div id="shipperInfo" class="text-muted">Đang tải tài khoản...</div></div><button class="btn btn-outline-primary" onclick="loadShipments()">Làm mới</button></div>
<div id="message"></div><div id="shipments" class="row g-3"></div>
</main>
<script>
var currentUser=null;
function esc(v){var s=v==null?'':String(v);return s.replace(/[&<>"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c];});}
function label(s){return {'ASSIGNED':'Đã phân công','PICKED_UP':'Đã lấy hàng','DELIVERING':'Đang giao','DELIVERED':'Đã giao','FAILED':'Giao thất bại'}[s]||s;}
function nextStatus(s){if(s==='ASSIGNED')return 'PICKED_UP';if(s==='PICKED_UP')return 'DELIVERING';if(s==='DELIVERING')return 'DELIVERED';return null;}
function nextText(s){if(s==='ASSIGNED')return 'Xác nhận đã nhận hàng';if(s==='PICKED_UP')return 'Bắt đầu giao hàng';if(s==='DELIVERING')return 'Xác nhận giao thành công';return '';}
function loadShipments(){
 if(!currentUser)return;
 fetch('/api/shipper/'+currentUser.id+'/shipments/active').then(function(r){if(!r.ok)throw new Error('Không tải được danh sách vận đơn');return r.json();}).then(function(list){
  var box=document.getElementById('shipments'); if(!list.length){box.innerHTML='<div class="col-12"><div class="alert alert-success">Hiện không có vận đơn đang xử lý.</div></div>';return;}
  box.innerHTML=list.map(function(sh){var next=nextStatus(sh.status);return '<div class="col-md-6 col-xl-4"><div class="page-card p-4 h-100"><div class="d-flex justify-content-between"><span class="badge text-bg-primary">'+esc(label(sh.status))+'</span><span class="small text-muted">#'+esc(sh.id)+'</span></div><h5 class="fw-bold mt-3">'+esc(sh.trackingCode)+'</h5><div class="small text-muted mb-2">Đơn hàng: #'+esc(sh.orderId||'—')+'</div><div class="small mb-3">Shipper phụ trách: <b>'+esc(currentUser.fullName||currentUser.username)+'</b></div>'+ (next?'<button class="btn btn-primary w-100" onclick="updateStatus('+sh.id+',\''+next+'\')">'+nextText(sh.status)+'</button>':'<div class="alert alert-success mb-0">Đã hoàn tất</div>')+'</div></div>';}).join('');
 }).catch(function(e){document.getElementById('message').innerHTML='<div class="alert alert-danger">'+esc(e.message)+'</div>';});
}
function updateStatus(shipmentId,status){
 fetch('/api/shipper/'+currentUser.id+'/shipments/'+shipmentId+'/status?status='+encodeURIComponent(status),{method:'PUT'}).then(function(r){if(!r.ok)return r.text().then(function(t){throw new Error(t||('HTTP '+r.status));});return r.json();}).then(function(){loadShipments();}).catch(function(e){document.getElementById('message').innerHTML='<div class="alert alert-danger">'+esc(e.message)+'</div>';});
}
fetch('/api/me').then(function(r){if(!r.ok)throw new Error('Vui lòng đăng nhập bằng tài khoản Shipper.');return r.json();}).then(function(me){currentUser=me;document.getElementById('shipperInfo').innerText=(me.fullName||me.username)+' • '+(me.role&&me.role.name||'SHIPPER');loadShipments();}).catch(function(e){document.getElementById('message').innerHTML='<div class="alert alert-danger">'+esc(e.message)+'</div>';});
</script>
</body>
</html>
