# PDF Maker
An API that allows you to create your own PDF like invoices, receipts, etc.

### Prerequisites

  - Apache Maven 3+
  - [Martini Desktop](https://www.torocloud.com/martini/download)

### Building the Martini Package

```
$ mvn clean package
```
This will create a ZIP file named `pdf-maker.zip` containing all the files (services, configurations, etc.) needed under the `target` folder. 
This ZIP file is what we call a [Martini Package](https://docs.torocloud.com/martini/latest/developing/package/) 
which then you can import into Martini Desktop to get started. You can learn more about how to import a Martini Package 
by visiting our [documentation](https://docs.torocloud.com/martini/latest/developing/package/importing/).

### Usage
This package exposes REST APIs for creating barcode image and listing all supported formats.
You can find the [Gloop REST API](https://docs.torocloud.com/martini/latest/developing/gloop/api/rest/) file 
at `/code/api/PdfMaker.api` after importing the package to your Martini Desktop application.

### Operations

The base url is `<host>/api/pdf-maker` where `host` is the location where the Martini instance is deployed. By default, it's `localhost:8080`.

`POST /create`

Creates a PDF based from the given template. 

**Request Body**

```json
{
  "pageMarginTop": 0.5,
  "pageMarginBottom": 0.5,
  "pageMarginRight": 0.5,
  "pageMarginLeft": 0.5,
  "pageSize": "LETTER",
  "body": []
}
```
* `pageMarginTop` - float value, page's top margin
* `pageMarginBottom` - float value, page's bottom margin
* `pageMarginRight` - float value, page's right margin
* `pageMarginLeft` - float value, page's left margin
* `pageSize` - the page size, values supported are LETTER, NOTE, LEGAL, TABLOID, EXECUTIVE, POSTCARD, A0, A1, A2, A3, A4, A7, A8, A10, LEDGER
* `body` - List of `Row` objects

**Row object**

Rows are like tables that consists of columns
```json
{
  "paddingTop": 2.0,
  "columns": []
}
```
* `paddingTop` - float value, top padding of each row
* `columns` - list of `Column` objects

**Column object**
```json
{
  "paddingTop": 2.0,
  "paddingBottom": 2.0,
  "paddingRight": 2.0,
  "paddingLeft": 2.0,
  "fixedHeight": 0.0,
  "indent": 0.0,
  "border": {},
  "element": {},
  "backgroundColor": {},
  "rows": []
}
```
* `paddingTop` - float value, column's top padding. Default value is 2.0.
* `paddingBottom` - float value, column's bottom padding. Default value is 2.0.
* `paddingRight` - float value, column's right padding. Default value is 2.0.
* `paddingLeft` - float value, column's left padding. Default value is 2.0.
* `fixedHeight` - float value, sets the fixed height of the `Column`
* `indent` - float value, identifies the indention of the element. Default value is 0.
* `border` - a `Border` element.
* `element` - an `Element` object. Sets the content of the `Column`
* `backgroundColor` - a `Color` object. Sets the background color of the `Column`
* `rows` - list of `Row` objects. Each `Column` may consist of multiple `Row` objects. (optional)

**Border object**
```json
{
  "width": 1.0,
  "top": true,
  "bottom": true,
  "right": true,
  "left": true,
  "baseColor": {}
}
```
* `width` - float value, sets the border thickness
* `top` - defaults to `false`. Set to `true` if you want to see top border.
* `bottom` - defaults to `false`.  Set to `true` if you want to see bottom border.
* `left` - defaults to `false`.  Set to `true` if you want to see left border.
* `right` - defaults to `false`.  Set to `true` if you want to see right border.
* `baseColor` - a `Color` object, sets the base color of the border.

**Color object**
```json
{
  "type": "BLACK",
  "red": 0,
  "green": 0,
  "blue": 0
}
```
* `type` - a set of available pre-defined colors WHITE, LIGHT_GRAY, GRAY, DARK_GRAY, BLACK, RED, PINK, ORANGE, YELLOW, GREEN, MAGENTA, CYAN, BLUE.
You can also select CUSTOM if you want to customize the color by supplying the RGB values.
* `red` - values from 0-255
* `green` - values from 0-255
* `blue` - values from 0-255

**Element object**
```json
{
  "type": "",
  "value": "",
  "font": {}
}
```
* `type` - the element type, either `TEXT` or `IMAGE`
* `value` - the text or image value
* `font` - a `Font` object, required only if the `type` is `TEXT`

**Font object**
```json
{
  "family": "HELVETICA",
   "size": 0.0,
   "style": "BOLD"
}
```
* `family` - the font family, available values are COURIER, HELVETICA, TIMES_ROMAN, SYMBOL, ZAPFDINGBATS. Defaults to HELVETICA.
* `size` - float value, size of the text element
* `style` - the font style, available values are NORMAL, BOLD, ITALIC, OBLIQUE, UNDERLINE, LINETHROUGH. Defaults to NORMAL.

**Sample Request**
Here's a sample request that creates a simple TODO table
**cURL**
```
curl --location --request POST 'http://localhost:8080/api/pdf-maker/create' \
--header 'Content-Type: application/json' \
--data-raw '{
	"pageMarginTop": 0.5,
	"pageMarginBottom": 0.5,
	"pageMarginRight": 0.5,
	"pageMarginLeft": 0.5,
	"pageSize": "LETTER",
	"body": [
	  {
	    "paddingTop": 2.0,
	    "columns": [
	      {
	        "paddingTop": 2.0,
	        "paddingBottom": 2.0,
	        "paddingRight": 2.0,
	        "paddingLeft": 2.0,
	        "fixedHeight": 0.0,
	        "indent": 0.0,
	        "border": {
	          "top": true,
	          "bottom": true,
	          "right": true,
	          "left": true,
	          "baseColor": {
	            "type": "GRAY"
	          }
	        },
	        "element": {
	          "type": "TEXT",
	          "value": "TODO",
	          "font": {
	            "family": "HELVETICA",
	            "size": 12.0,
	            "style": "BOLD",
	            "baseColor": {
	              "type": "LIGHT_GRAY"
	            }
	          }
	        },
	        "backgroundColor": {
	          "type": "BLACK"
	        },
	        "rows": [
	          {
	            "paddingTop": 2.0,
	            "columns": [
	              {
	                "paddingTop": 2.0,
	                "paddingBottom": 2.0,
	                "paddingRight": 2.0,
	                "paddingLeft": 2.0,
	                fixedHeight": 0.0,
	                "indent": 0.0,
	                "border": {
	                  "top": true,
	                  "bottom": true,
	                  right": true,
	                  "left": true
	                },
	                "element": {
	                  "type": "TEXT",
	                  "value": "Do laundry",
	                  "font": {
	                    "family": "HELVETICA",
	                    "size": 11.0,
	                    "style": "NORMAL"
	                  }
	                },
	                "rows": []
	              }
	            ]
	          },
	          {
	            "paddingTop": 2.0,
	            "columns": [
	              {
	                "paddingTop": 2.0,
	                "paddingBottom": 2.0,
	                "paddingRight": 2.0,
	                "paddingLeft": 2.0,
	                "fixedHeight": 0.0,
	                "indent": 0.0,
	                "border": {
	                  "top": true,
	                  "bottom": true,
	                  "right": true,
	                  "left": true
	                },
	                "element": {
	                  "type": "TEXT",
	                  "value": "Clean house",
	                  "font": {
	                    "family": "HELVETICA",
	                    "size": 11.0,
	                    "style": "NORMAL"
	                  }
	                },
	                "rows": []
	              }
	            ]
	          }
	        ]
	      }
	    ]
	  }
	]
}'
```
