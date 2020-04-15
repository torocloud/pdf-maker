package service.groovy

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.Utilities
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter

import io.toro.gloop.annotation.GloopObjectParameter
import io.toro.gloop.annotation.GloopParameter
import io.toro.gloop.object.property.GloopModel
import io.toro.martini.GroovyMethods
import io.toro.martini.LoggerMethods
import io.toro.martini.ipackage.MartiniPackage

import java.io.FileOutputStream

class PdfMaker {
    @GloopObjectParameter("output{\n  multipartFile#io.toro.martini.http.MultipartFile{\n  }\n}\n")
    public static GloopModel create( @GloopObjectParameter("template#model.PdfTemplate{\n}\n") GloopModel template) {

        if (template.body.isEmpty())
            return null

        MartiniPackage martiniPackage = GroovyMethods.getPackage()
        String id = UUID.randomUUID().toString()
        File pdfFile = new File("${martiniPackage.getInfo().home.toString()}/resources/${id}.pdf")
        try {

            float marginTop = Utilities.inchesToPoints(template.pageMarginTop)
            float marginBottom = Utilities.inchesToPoints(template.pageMarginBottom)
            float marginLeft = Utilities.inchesToPoints(template.pageMarginTop)
            float marginRight = Utilities.inchesToPoints(template.pageMarginLeft)
            Rectangle pageSize = PageSize.getRectangle(template.pageSize)
            Document document = new Document(pageSize,
                    marginLeft,
                    marginRight,
                    marginTop,
                    marginBottom)
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile))
            writer.setPdfVersion(PdfWriter.VERSION_1_7)
            document.open()

            template.body.each { body ->
                if (body.columns.isEmpty())
                    return

                def relativeWidths = []
                PdfPTable bodyTable = new PdfPTable(body.columns.size().intValue())
                bodyTable.setWidthPercentage(100)
                body.columns.eachWithIndex { innerColumn, index ->
                    PdfPTable table = new PdfPTable(1)
                    table.setWidthPercentage(100)
                    plotData(table, innerColumn)

                    if (innerColumn.relativeWidth != null && innerColumn.relativeWidth > 0) {
                        relativeWidths << innerColumn.relativeWidth
                    }

                    PdfPCell defaultCell = new PdfPCell(table)
                    defaultCell.setBorder(Rectangle.NO_BORDER)
                    defaultCell.setPaddingTop(body.paddingTop)
                    bodyTable.addCell(defaultCell)
                }

                if (relativeWidths.size() == body.columns.size().intValue()) {
                    bodyTable.setWidths((float[]) relativeWidths)
                }


                document.add(bodyTable)
            }

            document.close()

            Object temp = pdfFile
            GloopModel responseModel = new GloopModel('multipartFile')
            responseModel.setReference('io.toro.martini.http.MultipartFile')
            responseModel.put('size', temp.length())
            responseModel.put('contentType', 'application/pdf')
            responseModel.put('inputStream', temp)
            responseModel.put('originalFileName', "${id}.pdf")

            def output = new GloopModel('output')
            output.addChild(responseModel)

            return output
        } catch (Exception ex) {
            LoggerMethods.error('Unhandled exception while creating PDF', ex)
            throw ex
        } finally {
            pdfFile.delete()
        }
    }

    private static PdfPTable plotData(PdfPTable table, @GloopObjectParameter("column#model.Column{\n}\n") GloopModel column) {

        PdfPCell cell = null
        if (column.element.type == 'IMAGE') {
            Image image = Image.getInstance(column.element.value)
            image.scaleToFit(column.element.width, column.element.height)
            cell = new PdfPCell(image, false)

        } else if (column.element.type == 'TEXT') {
            Font font = getFont(column.element.font)
            cell = new PdfPCell(new Phrase(column.element.value, font))
        }

        if (cell != null) {
            addCellPadding(cell, column)
            addBorder(cell, column.border)

            if (!column.backgroundColor.isNullValue()) {
                cell.setBackgroundColor(getColor(column.backgroundColor))
            }

            //set cell fixed height
            cell.setFixedHeight(column.fixedHeight)

            //set element indention
            cell.setIndent(column.indent)

            //set horizontal alignment
            cell.setHorizontalAlignment(getAlignment(column.horizontalAlignment))

            //set vertical alignment
            cell.setVerticalAlignment(getAlignment(column.verticalAlignment))

            table.addCell(cell)
        }

        if (!column.rows.isEmpty()) {
            column.rows.each { it ->
                PdfPTable innerTable = new PdfPTable(it.columns.size().intValue())
                innerTable.setWidthPercentage(100)
                it.columns.each { innerCol ->
                    plotData(innerTable, innerCol)
                }

                PdfPCell defaultCell = new PdfPCell(innerTable)
                defaultCell.setBorder(Rectangle.NO_BORDER)
                table.addCell(defaultCell)
            }
        }

        return table
    }

    private static Font getFont(@GloopObjectParameter("templateFont#model.Font{\n}\n") GloopModel templateFont) {

        Font.FontFamily fontFamily
        if (templateFont.family == 'COURIER') {
            fontFamily = Font.FontFamily.COURIER
        } else if (templateFont.family == 'TIMES_ROMAN') {
            fontFamily = Font.FontFamily.TIMES_ROMAN
        } else if (templateFont.family == 'SYMBOL') {
            fontFamily = Font.FontFamily.SYMBOL
        } else if (templateFont.family == 'ZAPFDINGBATS') {
            fontFamily = Font.FontFamily.ZAPFDINGBATS
        } else {
            fontFamily = Font.FontFamily.HELVETICA
        }

        Font font = new Font(fontFamily, templateFont.size)
        if (templateFont.style != null && templateFont.style != '') {
            font.setStyle(templateFont.style)
        }

        if (templateFont.baseColor != null && !templateFont.baseColor.isNullValue()) {
            font.setColor(getColor(templateFont.baseColor))
        }

        return font
    }

    private static void addBorder(PdfPCell cell, @GloopObjectParameter("border#model.Border{\n}\n") GloopModel border) {

        if (!border.isNullValue()) {
            int borderLine = Rectangle.NO_BORDER
            borderLine = borderLine | (border.top ? Rectangle.TOP : 0)
            borderLine = borderLine | (border.bottom ? Rectangle.BOTTOM : 0)
            borderLine = borderLine | (border.right ? Rectangle.RIGHT : 0)
            borderLine = borderLine | (border.left ? Rectangle.LEFT : 0)

            cell.setBorder(borderLine)
            cell.setBorderColor(getColor(border.baseColor))

            if (border.width != null) {
                cell.setBorderWidth(border.width)
            }
        } else {
            cell.setBorder(Rectangle.NO_BORDER)
        }
    }

    private static void addCellPadding(PdfPCell cell, @GloopObjectParameter("column#model.Column{\n}\n") GloopModel column) {

        if (column.paddingTop != null) {
            cell.setPaddingTop(column.paddingTop)
        }

        if (column.paddingBottom != null) {
            cell.setPaddingBottom(column.paddingBottom)
        }

        if (column.paddingRight != null) {
            cell.setPaddingRight(column.paddingRight)
        }

        if (column.paddingLeft != null) {
            cell.setPaddingLeft(column.paddingLeft)
        }
    }

    private static Integer getAlignment(String alignment) {
        if (alignment == 'TOP') {
            return Element.ALIGN_TOP
        } else if (alignment == 'BOTTOM') {
            return Element.ALIGN_BOTTOM
        } else if (alignment == 'RIGHT') {
            return Element.ALIGN_RIGHT
        } else if (alignment == 'CENTER') {
            return Element.ALIGN_CENTER
        } else {
            //default
            return Element.ALIGN_LEFT
        }
    }

    private static BaseColor getColor(@GloopObjectParameter("baseColor#model.Color{\n}\n") GloopModel baseColor) {

        if (baseColor.type == 'CUSTOM') {
            return new BaseColor(baseColor.red, baseColor.green, baseColor.blue)
        } else if (baseColor.type == 'WHITE') {
            return BaseColor.WHITE
        } else if (baseColor.type == 'LIGHT_GRAY') {
            return BaseColor.LIGHT_GRAY
        } else if (baseColor.type == 'GRAY') {
            return BaseColor.GRAY
        } else if (baseColor.type == 'DARK_GRAY') {
            return BaseColor.DARK_GRAY
        } else if (baseColor.type == 'RED') {
            return BaseColor.RED
        } else if (baseColor.type == 'PINK') {
            return BaseColor.PINK
        } else if (baseColor.type == 'ORANGE') {
            return BaseColor.ORANGE
        } else if (baseColor.type == 'YELLOW') {
            return BaseColor.YELLOW
        } else if (baseColor.type == 'GREEN') {
            return BaseColor.GREEN
        } else if (baseColor.type == 'MAGENTA') {
            return BaseColor.MAGENTA
        } else if (baseColor.type == 'CYAN') {
            return BaseColor.CYAN
        } else if (baseColor.type == 'BLUE') {
            return BaseColor.BLUE
        } else {
            return BaseColor.BLACK
        }

    }

}
