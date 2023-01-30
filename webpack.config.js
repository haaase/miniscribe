const CompressionPlugin = require("compression-webpack-plugin");
const zlib = require("zlib");
const { merge } = require("webpack-merge");
var webpack = require('webpack');
const ScalaJS = require("./scalajs.webpack.config");

module.exports = merge(ScalaJS, {
    "plugins": [
        new CompressionPlugin({
            algorithm: "brotliCompress",
            filename: "[base].br",
            test: /\.js$/,
            compressionOptions: {
                params: {
                    [zlib.constants.BROTLI_PARAM_QUALITY]: 11,
                },
            },
            minRatio: 0.8,
            deleteOriginalAssets: false,
        }),
        new CompressionPlugin({
            algorithm: "gzip",
            filename: "[base].gzip",
            test: /\.js$/,
            compressionOptions: {
                level: 9
            },
            minRatio: 0.8,
            deleteOriginalAssets: false,
        }),
    ]
})