#!/bin/bash

# AI智能小说创作系统 - 云服务器停止脚本

echo "========================================="
echo "⏹️  停止 AI智能小说创作系统"
echo "========================================="

# 切换到脚本所在目录
cd "$(dirname "$0")"
WORK_DIR=$(pwd)

PID_FILE="${WORK_DIR}/app.pid"
JAR_NAME="ai-novel-writer.jar"

# 从PID文件停止
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "🔄 停止进程 (PID: $PID)..."
        sudo kill "$PID"
        
        # 等待进程结束
        for i in {1..30}; do
            if ! ps -p "$PID" > /dev/null 2>&1; then
                echo "✅ 应用已停止"
                rm -f "$PID_FILE"
                exit 0
            fi
            echo -n "."
            sleep 1
        done
        
        # 强制停止
        echo ""
        echo "⚠️  进程未响应，强制停止..."
        sudo kill -9 "$PID"
        sleep 2
        
        if ! ps -p "$PID" > /dev/null 2>&1; then
            echo "✅ 应用已强制停止"
            rm -f "$PID_FILE"
        else
            echo "❌ 无法停止进程"
            exit 1
        fi
    else
        echo "ℹ️  PID文件存在但进程不在运行"
        rm -f "$PID_FILE"
    fi
fi

# 通过进程名停止（备用方案）
PIDS=$(pgrep -f "$JAR_NAME")
if [ -n "$PIDS" ]; then
    echo "🔄 发现运行中的进程，停止中..."
    for pid in $PIDS; do
        echo "   停止 PID: $pid"
        sudo kill "$pid"
    done
    sleep 3
    
    # 检查是否还在运行
    PIDS=$(pgrep -f "$JAR_NAME")
    if [ -n "$PIDS" ]; then
        echo "⚠️  强制停止残留进程..."
        for pid in $PIDS; do
            sudo kill -9 "$pid"
        done
    fi
    echo "✅ 应用已停止"
else
    echo "ℹ️  没有找到运行中的应用"
fi

echo "========================================="
