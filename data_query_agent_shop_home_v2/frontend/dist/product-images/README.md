# 商品图片放置说明

每个商品用商品 ID 建一个文件夹。假设商品 ID 是 `12`：

```text
product-images/
├── manifest.json
└── 12/
    ├── cover.jpg
    ├── detail-1.jpg
    └── detail-2.jpg
```

然后在 `manifest.json` 中登记图片：

```json
{
  "12": ["cover.jpg", "detail-1.jpg", "detail-2.jpg"]
}
```

第一张图会作为列表封面，全部图片会出现在商品详情弹窗中。支持 jpg、jpeg、png、webp 等格式。
