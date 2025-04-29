import { generateService } from '@umijs/openapi'

generateService({
  requestLibPath: "import request from '@/request.ts'",
  // swagger 接口文档地址
  schemaPath: 'http://localhost:8982/api/v2/api-docs',
  // 存放的目录
  serversPath: './src',
})
