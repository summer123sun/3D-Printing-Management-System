#!/usr/bin/env bash
# =====================================================
# pre-commit 钩子：检查敏感文件不含真实密码/密钥
# 用法：把这个文件复制到 .git/hooks/pre-commit
#   或在仓库根执行：cp scripts/pre-commit-check.sh .git/hooks/pre-commit
#                    chmod +x .git/hooks/pre-commit
# =====================================================

# 检测 application-dev.yml 是否含真实密码
YML="spring-boot-backend/src/main/resources/application-dev.yml"

if [ -f "$YML" ]; then
    if grep -qE "password:[[:space:]]*[^$\{][^[:space:]]+" "$YML"; then
        echo "========================================"
        echo "[!] 检测到 application-dev.yml 含真实密码"
        echo "========================================"
        echo ""
        echo "问题：password 字段没有用 \${DB_PASSWORD:xxx} 占位符"
        echo "位置：$YML"
        echo ""
        echo "修复：把 password 改回占位符"
        echo "  password: Xy@20070831   →   password: \${DB_PASSWORD:root}"
        echo ""
        exit 1
    fi
fi

# 检测 .env 是否含真实密钥（前端）
if [ -f "vue-3d/.env" ] || [ -f "vue-3d/.env.production" ]; then
    for envfile in vue-3d/.env vue-3d/.env.production vue-3d/.env.development; do
        if [ -f "$envfile" ]; then
            if grep -qE "=sk-|=pk-|=AKIA|secret[_-]?key" "$envfile" 2>/dev/null; then
                echo "[!] $envfile 检测到可能的密钥，请检查"
                exit 1
            fi
        fi
    done
fi

# 检测是否有 .env.local / application-local.yml
for f in vue-3d/.env.local vue-3d/.env.*.local spring-boot-backend/src/main/resources/application-local.yml; do
    if [ -f "$f" ]; then
        echo "[!] $f 不应该提交到 git"
        exit 1
    fi
done

exit 0
