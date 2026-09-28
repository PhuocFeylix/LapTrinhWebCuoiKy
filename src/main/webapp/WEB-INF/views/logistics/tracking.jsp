<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Tra cứu vận đơn - UTEExpress</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="/resources/css/style.css" rel="stylesheet">
<style>
.tracking-hero{background:linear-gradient(135deg,#eaf3ff,#fff);border-radius:24px;padding:42px 30px}
.timeline{position:relative;margin:28px 0 0 18px;padding-left:30px;border-left:3px solid #dbe5f4}
.timeline-item{position:relative;padding:0 0 28px}
.timeline-item:before{content:'';position:absolute;left:-41px;top:3px;width:20px;height:20px;border-radius:50%;background:#fff;border:4px solid #adb5bd}
.timeline-item.done:before{border-color:#198754;background:#198754}
.timeline-item.current:before{border-color:#0d6efd;background:#0d6efd}
.status-box{border-radius:16px;padding:18px;background:#f8fafc}
</style>
</head>
<body>
<nav class="navbar navbar-dark navbar-ute"><div class="container"><a class="navbar-brand fw-bold" href="/">UTEExpress</a><span class="text-white">Tra cứu vận đơn</span></div></nav>
<main class="container py-5">
<div class="tracking-hero mb-4">
<h1 class="fw-bold">Tra cứu vận đơn</h1>
<p class="text-muted mb-4">Nhập mã vận đơn UTEExpress để xem trạng thái giao hàng.</p>
<div class="row g-2">
<div class="col-md-9"><input id="trackingCode" class="form-control form-control-lg" placeholder="Ví dụ: UTEABC123456789"></div>
<div class="col-md-3"><button id="btnTrack" class="btn btn-primary btn-lg w-100">Tra cứu</button></div>
</div>
</div>
<div id="message"></div>
<div id="result" class="page-card p-4 d-none"></div>
</main>
<script>
(function(){
  var input=document.getElementById('trackingCode');
  var button=document.getElementById('btnTrack');
  var result=document.getElementById('result');
  var message=document.getElementById('message');
  function esc(v){var s=v==null?'':String(v);return s.replace(/[&<>"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c];});}
  function fmt(v){if(!v)return '—';return new Date(v).toLocaleString('vi-VN');}
  function label(s){return {'READY':'Chờ phân công','ASSIGNED':'Đã phân công Shipper','PICKED_UP':'Đã lấy hàng','DELIVERING':'Đang giao hàng','DELIVERED':'Giao thành công','FAILED':'Giao thất bại','CANCELLED':'Đã hủy'}[s]||s;}
  function render(sh){
    var status=sh.status||'READY';
    var stages=[
      ['READY','Tạo vận đơn',sh.createdAt],
      ['ASSIGNED','Phân công Shipper',sh.assignedAt],
      ['PICKED_UP','Shipper đã nhận hàng',sh.pickedUpAt],
      ['DELIVERING','Đang giao hàng',null],
      ['DELIVERED','Giao thành công',sh.deliveredAt]
    ];
    var order={READY:0,ASSIGNED:1,PICKED_UP:2,DELIVERING:3,DELIVERED:4,FAILED:3,CANCELLED:3};
    var idx=order[status];
    var html='<div class="d-flex justify-content-between align-items-start flex-wrap gap-3">'+
      '<div><div class="text-muted small">MÃ VẬN ĐƠN</div><h3 class="fw-bold">'+esc(sh.trackingCode)+'</h3></div>'+
      '<span class="badge text-bg-primary fs-6">'+esc(label(status))+'</span></div>'+
      '<div class="row g-3 mt-2"><div class="col-md-4"><div class="status-box"><small class="text-muted">Shipment ID</small><div class="fw-bold">#'+esc(sh.id)+'</div></div></div>'+
      '<div class="col-md-4"><div class="status-box"><small class="text-muted">Shipper</small><div class="fw-bold">'+esc(sh.shipperName||'Chưa phân công')+'</div></div></div>'+
      '<div class="col-md-4"><div class="status-box"><small class="text-muted">Ngày tạo</small><div class="fw-bold">'+fmt(sh.createdAt)+'</div></div></div></div>'+
      '<div class="timeline">';
    stages.forEach(function(x,i){var done=i<idx||status==='DELIVERED'&&i<=4;var current=i===idx;html+='<div class="timeline-item '+(done?'done ':'')+(current?'current':'')+'"><div class="fw-bold">'+esc(x[1])+'</div><div class="text-muted small">'+(x[2]?fmt(x[2]):(current?label(status):'Chưa cập nhật'))+'</div></div>';});
    html+='</div>';
    if(status==='FAILED'||status==='CANCELLED') html+='<div class="alert alert-warning mt-3"><strong>'+esc(label(status))+'</strong>. '+esc(sh.note||'Vận đơn không tiếp tục theo quy trình thông thường.')+'</div>';
    result.innerHTML=html; result.classList.remove('d-none');
  }
  function track(){
    var code=input.value.trim(); if(!code){message.innerHTML='<div class="alert alert-warning">Vui lòng nhập mã vận đơn.</div>';return;}
    message.innerHTML=''; result.classList.add('d-none'); button.disabled=true; button.innerText='Đang tra cứu...';
    fetch('/api/shipments/tracking/'+encodeURIComponent(code)).then(function(r){if(!r.ok)throw new Error('Không tìm thấy vận đơn');return r.json();}).then(render).catch(function(e){message.innerHTML='<div class="alert alert-danger">'+esc(e.message)+'</div>';}).finally(function(){button.disabled=false;button.innerText='Tra cứu';});
  }
  button.addEventListener('click',track); input.addEventListener('keydown',function(e){if(e.key==='Enter')track();});
  var code=new URLSearchParams(window.location.search).get('code'); if(code){input.value=code;track();}
})();
</script>
</body>
</html>
