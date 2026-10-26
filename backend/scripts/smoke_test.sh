#!/usr/bin/env bash
# =====================================================================
# 后端接口冒烟测试脚本
# 用法：bash scripts/smoke_test.sh [http://localhost:8080]
# 依赖：curl、python3
# =====================================================================
H=${1:-http://localhost:8080}
PASS=0; FAIL=0

# check <期望HTTP码> <期望业务code> <描述> <curl参数...>
check() {
  local exp_http=$1 exp_code=$2 name=$3; shift 3
  local http
  http=$(curl -s -o /tmp/smoke.json -w "%{http_code}" "$@")
  local code
  code=$(python3 -c "import json;print(json.load(open('/tmp/smoke.json')).get('code'))" 2>/dev/null || echo "-")
  if [[ "$http" == "$exp_http" && "$code" == "$exp_code" ]]; then
    PASS=$((PASS+1)); printf "  [PASS] %-34s HTTP %s code=%s\n" "$name" "$http" "$code"
  else
    FAIL=$((FAIL+1)); printf "  [FAIL] %-34s HTTP %s code=%s  %s\n" "$name" "$http" "$code" "$(head -c 200 /tmp/smoke.json)"
  fi
}

login() {
  curl -s -X POST "$H/api/auth/login" -H 'Content-Type: application/json' \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['token'])"
}

echo "== 认证 =="
ADMIN=$(login admin admin123)
VISITOR=$(login visitor visitor123)
A=(-H "Authorization: Bearer $ADMIN")
V=(-H "Authorization: Bearer $VISITOR")
J=(-H 'Content-Type: application/json')
check 200 200 "登录 admin" -X POST "$H/api/auth/login" "${J[@]}" -d '{"username":"admin","password":"admin123"}'
check 200 401 "错误密码" -X POST "$H/api/auth/login" "${J[@]}" -d '{"username":"admin","password":"wrong"}'
check 200 400 "登录参数校验" -X POST "$H/api/auth/login" "${J[@]}" -d '{"username":""}'
RU="smoke$RANDOM"
check 200 200 "注册新游客" -X POST "$H/api/auth/register" "${J[@]}" -d "{\"username\":\"$RU\",\"password\":\"123456\",\"nickname\":\"冒烟测试\"}"
check 200 200 "当前用户 /me" "$H/api/auth/me" "${A[@]}"
check 401 401 "未登录访问 /me" "$H/api/auth/me"

echo "== 文物 =="
check 200 200 "文物分页 /page" "$H/api/artifacts/page?current=1&size=5"
check 200 200 "文物多条件检索" -G "$H/api/artifacts/search" --data-urlencode "keyword=青铜" -d pitId=2 -d orderBy=hot
check 200 200 "文物详情" "$H/api/artifacts/1/detail"
check 200 404 "文物不存在" "$H/api/artifacts/9999/detail"
check 200 200 "热门文物" "$H/api/artifacts/hot?limit=5"
check 200 200 "分类列表" "$H/api/categories/list"
check 200 200 "祭祀坑分页" "$H/api/pits/page"
check 200 200 "3DGS模型列表" "$H/api/models/list?artifactId=1"
check 200 200 "展厅列表" "$H/api/halls/list"
check 200 200 "文献搜索" -G "$H/api/literature/page" --data-urlencode "keyword=蚕丛"
check 200 403 "游客新增文物被拒" -X POST "$H/api/artifacts" "${V[@]}" "${J[@]}" -d '{"artifactNo":"T-1","name":"测试"}'
check 200 200 "管理员新增文物" -X POST "$H/api/artifacts" "${A[@]}" "${J[@]}" -d "{\"artifactNo\":\"T-$RANDOM\",\"name\":\"冒烟测试文物\",\"categoryId\":1,\"pitId\":1,\"materialId\":1}"
NEW_ID=$(python3 -c "import json;print(json.load(open('/tmp/smoke.json'))['data']['id'])")
check 200 200 "管理员修改文物" -X PUT "$H/api/artifacts/$NEW_ID" "${A[@]}" "${J[@]}" -d '{"name":"冒烟测试文物-改","artifactNo":"T-X'$RANDOM'"}'
check 200 200 "管理员删除文物(逻辑)" -X DELETE "$H/api/artifacts/$NEW_ID" "${A[@]}"

echo "== 知识图谱 =="
check 200 200 "全图" "$H/api/kg/graph?limit=100"
check 200 200 "按类型过滤" "$H/api/kg/graph?types=ARTIFACT,PIT"
check 200 200 "2跳邻居" "$H/api/kg/neighbors/1?hops=2"
check 200 200 "最短路径" "$H/api/kg/path?fromId=2&toId=5"
check 200 200 "实体搜索" -G "$H/api/kg/search" --data-urlencode "keyword=神树"
check 200 200 "图谱统计" "$H/api/kg/statistics"
check 200 200 "实体分页(CRUD)" "$H/api/kg/entities/page?entityType=PIT"

echo "== 数据大屏 =="
check 200 200 "总览" "$H/api/dashboard/overview"
check 200 200 "分布(全部)" "$H/api/dashboard/artifact-distribution/all"
check 200 200 "分布(材质)" "$H/api/dashboard/artifact-distribution?dimension=material"
check 200 200 "客流趋势" "$H/api/dashboard/visit-trend?days=30"
check 200 200 "问答统计" "$H/api/dashboard/qa"
check 200 200 "模型统计" "$H/api/dashboard/models"
check 200 200 "服务统计" "$H/api/dashboard/services"

echo "== GraphRAG 问答 =="
check 200 200 "同步问答(匿名)" -X POST "$H/api/qa/ask" "${J[@]}" -d '{"question":"青铜神树出土于哪个祭祀坑？有什么含义？"}'
SID=$(python3 -c "import json;print(json.load(open('/tmp/smoke.json'))['data']['sessionId'])")
check 200 200 "多轮追问(同会话)" -X POST "$H/api/qa/ask" "${J[@]}" -d "{\"sessionId\":$SID,\"question\":\"它和太阳崇拜有什么关系？\"}"
check 200 200 "登录用户问答" -X POST "$H/api/qa/ask" "${V[@]}" "${J[@]}" -d '{"question":"纵目面具和蚕丛有什么关系？"}'
check 200 200 "我的会话" "$H/api/qa/sessions" "${V[@]}"
check 200 200 "仅检索" -G "$H/api/qa/retrieve" --data-urlencode "question=五号坑出土了什么"
check 200 200 "大模型状态" "$H/api/qa/llm-status"
check 200 400 "空问题校验" -X POST "$H/api/qa/ask" "${J[@]}" -d '{"question":""}'
EVENTS=$(curl -s -N -X POST "$H/api/qa/stream" "${J[@]}" -d '{"question":"金杖上刻了什么图案？"}' --max-time 60 | grep -c '^event:')
if [[ "$EVENTS" -gt 2 ]]; then PASS=$((PASS+1)); echo "  [PASS] SSE流式问答                       收到 $EVENTS 个事件"; else FAIL=$((FAIL+1)); echo "  [FAIL] SSE流式问答 事件数=$EVENTS"; fi

echo "== 预约 / 评价 / 收藏 / 路线 =="
TOMORROW=$(python3 -c "import datetime;print(datetime.date.today()+datetime.timedelta(days=3))")
check 200 200 "时段余量(公开)" "$H/api/reservations/slots?date=$TOMORROW"
check 200 200 "提交预约" -X POST "$H/api/reservations" "${V[@]}" "${J[@]}" -d "{\"visitDate\":\"$TOMORROW\",\"timeSlot\":\"13:00-15:00\",\"visitorCount\":2,\"contactName\":\"演示游客\",\"contactPhone\":\"13800000003\"}"
RID=$(python3 -c "import json;print(json.load(open('/tmp/smoke.json'))['data']['id'])")
check 200 409 "重复预约被拒" -X POST "$H/api/reservations" "${V[@]}" "${J[@]}" -d "{\"visitDate\":\"$TOMORROW\",\"timeSlot\":\"13:00-15:00\",\"visitorCount\":2,\"contactName\":\"演示游客\",\"contactPhone\":\"13800000003\"}"
check 200 200 "我的预约" "$H/api/reservations/my" "${V[@]}"
check 200 200 "取消预约" -X PUT "$H/api/reservations/$RID/cancel" "${V[@]}"
check 200 200 "管理端预约分页" "$H/api/reservations/page" "${A[@]}"
check 200 200 "评价列表(公开)" "$H/api/reviews/target?targetType=ARTIFACT&targetId=1"
check 200 200 "评分汇总" "$H/api/reviews/summary?targetType=ARTIFACT&targetId=1"
check 200 200 "发表评价" -X POST "$H/api/reviews" "${V[@]}" "${J[@]}" -d '{"targetType":"ARTIFACT","targetId":11,"rating":5,"content":"太阳形器很震撼"}'
check 200 409 "重复评价被拒" -X POST "$H/api/reviews" "${V[@]}" "${J[@]}" -d '{"targetType":"ARTIFACT","targetId":11,"rating":4}'
check 200 200 "收藏切换" -X POST "$H/api/favorites/toggle" "${V[@]}" "${J[@]}" -d '{"targetType":"ARTIFACT","targetId":3}'
check 200 200 "我收藏的文物" "$H/api/favorites/artifacts" "${V[@]}"
check 200 200 "路线列表" "$H/api/routes/list"
check 200 200 "路线详情" "$H/api/routes/1/detail"
check 200 200 "路线推荐" -G "$H/api/routes/recommend" --data-urlencode "interests=面具" -d minutes=60
check 200 200 "开始导览(匿名)" -X POST "$H/api/routes/1/start"

echo "== 系统管理 =="
check 200 200 "用户分页(管理员)" "$H/api/users/page" "${A[@]}"
check 200 403 "用户分页(游客被拒)" "$H/api/users/page" "${V[@]}"
check 200 200 "系统配置(脱敏)" "$H/api/configs?group=LLM" "${A[@]}"
check 200 200 "操作日志" "$H/api/logs/page" "${A[@]}"
check 200 200 "角色列表" "$H/api/roles/list" "${A[@]}"
check 200 200 "公告列表" "$H/api/announcements/list"
check 200 200 "展览活动" "$H/api/events/page"
check 200 "-" "Knife4j 文档页" "$H/doc.html"
check 200 "None" "OpenAPI JSON" "$H/v3/api-docs/default"

echo
echo "通过 $PASS 项，失败 $FAIL 项"
[[ $FAIL -eq 0 ]]
