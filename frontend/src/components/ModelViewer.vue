<template>
  <GltfViewer
    v-if="!demo && model && isMeshModel(model)"
    :key="'g' + model.id"
    :url="model.modelUrl"
    :format="model.format"
    :author="model.author"
    :source-url="model.sourceUrl"
    :license="model.license"
    :model-title="model.name"
    :show-tips="showTips"
    :fit-width="fitWidth"
    @status="(s: string) => emit('status', s)"
  />
  <SplatViewer
    v-else
    :key="demo ? 'demo' : 's' + (model?.id ?? 'none')"
    :url="demo ? null : model?.modelUrl"
    :demo="demo"
    :camera-position="parseVec(model?.cameraPosition)"
    :camera-target="parseVec(model?.cameraTarget)"
    :format="demo ? 'splat' : model?.format"
    :show-tips="showTips"
    :pending-text="model ? undefined : '该文物暂无三维模型，正在规划采集，完成后将在此展示真实模型'"
    @status="(s: string) => emit('status', s)"
  />
</template>

<script setup lang="ts">
/**
 * 统一的三维查看器：按模型格式选择加载器
 *  - glb / gltf：three.js GLTFLoader（真实三维扫描网格）
 *  - splat / ksplat / ply / spz：3D 高斯泼溅查看器（文件不可访问时显示“模型采集中”）
 *  - demo：公开 3DGS 示例场景，明确标注为技术演示
 */
import GltfViewer from './GltfViewer.vue'
import SplatViewer from './SplatViewer.vue'
import type { Model3d } from '@/api/types'
import { isMeshModel, parseVec } from '@/utils/format'

withDefaults(defineProps<{ model?: Model3d | null; demo?: boolean; showTips?: boolean; fitWidth?: number }>(), { demo: false, showTips: true, fitWidth: 1 })
const emit = defineEmits<{ (e: 'status', s: string): void }>()
</script>
