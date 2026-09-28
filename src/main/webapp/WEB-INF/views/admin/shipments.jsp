<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Điều phối vận đơn - UTEExpress</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><link href="/resources/css/style.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-dark navbar-ute"><div class="container"><a class="navbar-brand fw-bold" href="/">UTEExpress</a><span class="text-white">Điều phối vận đơn</span></div></nav>
<main class="container py-4">
<div class="row g-3 mb-4">
<div class="col-md-4"><div class="page-card p-4"><div class="text-muted">Chưa phân công</div><div id="countUnassigned" class="display-6 fw-bold">0</div></div></div>
<div class="col-md-4"><div class="page-card p-4"><div class="text-muted">Shipper khả dụng</div><div id="countShippers" class="display-6 fw-bold">0</div></div></div>
<div class="col-md-4"><div class="page-card p-4"><div class="text-muted">Quy trình</div><div class="fw-bold mt-2">READY → ASSIGNED → PICKED_UP → DELIVERING → DELIVERED</div></div></div>
</div>
<div id="message"></div>
<div class="page-card p-4 mb-4"><div class="d-flex justify-content-between align-items-center"><h4 class="fw-bold mb-0">Tạo vận đơn</h4><span class="small text-muted">ShopOrder phải ở READY_TO_SHIP</span></div><div class="row g-2 mt-2"><div class="col-md-8"><input id="shopOrderId" class="form-control" type="number" placeholder="Nhập ShopOrder ID"></div><div class="col-md-4"><button class="btn btn-primary w-100" onclick="createShipment()">Tạo vận đơn</button></div></div></div>
<div class="page-card p-4"><div class="d-flex justify-content-between align-items-center mb-3"><h4 class="fw-bold mb-0">Vận đơn chờ phân công</h4><button class="btn btn-outline-primary btn-sm" onclick="loadAll()">Làm mới</button></div><div class="table-responsive"><table class="table align-middle"><thead><tr><th>Mã vận đơn</th><th>Shipment</th><th>Đơn hàng</th><th>Trạng thái</th><th>Phân công</th></tr></thead><tbody id="rows"></tbody></table></div></div>
</main>
<script>
var shippers=[];
function esc(v){var s=v==null?'':String(v);return s.replace(/[&<>"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c];});}
function loadAll(){Promise.all([fetch('/api/admin/shipments/unassigned').then(function(r){if(!r.ok)throw new Error('Không tải được vận đơn');return r.json();}),fetch('/api/admin/shipments/shippers').then(function(r){if(!r.ok)throw new Error('Không tải được danh sách Shipper');return r.json();})]).then(function(data){var list=data[0];shippers=data[1];document.getElementById('countUnassigned').innerText=list.length;document.getElementById('countShippers').innerText=shippers.length;var rows=document.getElementById('rows');if(!list.length){rows.innerHTML='<tr><td colspan="5" class="text-center text-muted py-4">Không có vận đơn chờ phân công.</td></tr>';return;}rows.innerHTML=list.map(function(sh){var opts=shippers.map(function(u){return '<option value="'+esc(u.id)+'">'+esc(u.fullName||u.username)+'</option>';}).join('');return '<tr><td class="fw-bold">'+esc(sh.trackingCode)+'</td><td>#'+esc(sh.id)+'</td><td>#'+esc(sh.orderId||'—')+'</td><td><span class="badge text-bg-warning">READY</span></td><td><div class="input-group"><select id="shipper-'+sh.id+'" class="form-select"><option value="">Chọn Shipper</option>'+opts+'</select><button class="btn btn-primary" onclick="assign('+sh.id+')">Gán</button></div></td></tr>';}).join('');}).catch(function(e){document.getElementById('message').innerHTML='<div class="alert alert-danger">'+esc(e.message)+'</div>';});}
function createShipment(){var id=document.getElementById('shopOrderId').value;if(!id){alert('Nhập ShopOrder ID');return;}fetch('/api/admin/shipments/shop-order/'+encodeURIComponent(id),{method:'POST'}).then(function(r){if(!r.ok)return r.text().then(function(t){throw new Error(t||('HTTP '+r.status));});return r.json();}).then(function(sh){document.getElementById('message').innerHTML='<div class="alert alert-success">Đã tạo vận đơn <b>'+esc(sh.trackingCode)+'</b> cho ShopOrder #'+esc(id)+'.</div>';document.getElementById('shopOrderId').value='';loadAll();}).catch(function(e){document.getElementById('message').innerHTML='<div class="alert alert-danger">'+esc(e.message)+'</div>';});}
function assign(shipmentId){var id=document.getElementById('shipper-'+shipmentId).value;if(!id){alert('Chọn Shipper');return;}fetch('/api/admin/shipments/'+shipmentId+'/assign/'+id,{method:'PUT'}).then(function(r){if(!r.ok)return r.text().then(function(t){throw new Error(t||('HTTP '+r.status));});return r.json();}).then(function(sh){document.getElementById('message').innerHTML='<div class="alert alert-success">Đã phân công '+esc(sh.trackingCode)+'. Trạng thái chuyển sang ASSIGNED.</div>';loadAll();}).catch(function(e){document.getElementById('message').innerHTML='<div class="alert alert-danger">'+esc(e.message)+'</div>';});}
loadAll();
</script>
</body>
</html>
