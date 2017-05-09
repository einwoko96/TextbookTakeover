package com.app.buynow;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.textbooktakeover.ChatActivity;
import com.app.textbooktakeover.TextbookTakeoverApplication;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by hitasoft on 16/9/16.
 **/

public class OrderDetail extends AppCompatActivity implements View.OnClickListener {

    ImageView backBtn, settingbtn, itemImage, titleimage, printbtn;
    TextView itemName, itemPrice, chatseller, orderstatus, title, orderid, orderDateTxt, shippedDateTxt, deliveryDateTxt;
    TextView sellername, selrtext, paymenttype, transactionid, orderdate, shipdate, deliverydate, price, shipingprice, total,
               nickName, fullName, addressLine, city, country, reviewTitle, ratingCount, review, editReview, reviewDate;
    HashMap<String, String> data = new HashMap<>();
    String odate="", sdate="", ddate="";
    RelativeLayout main, reviewLay;
    LinearLayout writeReviewLay;
    Display display;
    String from;
    int position;
    String chatId = "", pdfName;
    public static int topPadding, leftPadding;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        printbtn = (ImageView) findViewById(R.id.printbtn);
        itemImage = (ImageView) findViewById(R.id.imageView);
        titleimage = (ImageView) findViewById(R.id.userImg);
        settingbtn = (ImageView) findViewById(R.id.settingbtn);
        main = (RelativeLayout) findViewById(R.id.main);
        title = (TextView) findViewById(R.id.username);
        itemName = (TextView) findViewById(R.id.itemtitle);
        chatseller = (TextView) findViewById(R.id.chatseller);
        itemPrice = (TextView) findViewById(R.id.itemprice);
        orderstatus = (TextView) findViewById(R.id.orderstatus);
        sellername = (TextView) findViewById(R.id.selrname);
        selrtext = (TextView) findViewById(R.id.selrtext);
        paymenttype = (TextView) findViewById(R.id.paymenttype);
        transactionid = (TextView) findViewById(R.id.transid);
        orderdate = (TextView) findViewById(R.id.orderdate);
        shipdate = (TextView) findViewById(R.id.shipeddate);
        deliverydate = (TextView) findViewById(R.id.deliverydate);
        price = (TextView) findViewById(R.id.price);
        shipingprice = (TextView) findViewById(R.id.shipingfee);
        total = (TextView) findViewById(R.id.ordertotal);
        orderid = (TextView) findViewById(R.id.orderid);
        nickName = (TextView) findViewById(R.id.nickName);
        fullName = (TextView) findViewById(R.id.fullName);
        addressLine = (TextView) findViewById(R.id.addressLine);
        city = (TextView) findViewById(R.id.city);
        country = (TextView) findViewById(R.id.country);
        orderDateTxt = (TextView) findViewById(R.id.order_date_txt);
        shippedDateTxt = (TextView) findViewById(R.id.shipped_date_txt);
        deliveryDateTxt = (TextView) findViewById(R.id.delivery_date_txt);
        reviewTitle = (TextView) findViewById(R.id.reviewTitle);
        ratingCount = (TextView) findViewById(R.id.ratingCount);
        review = (TextView) findViewById(R.id.review);
        editReview = (TextView) findViewById(R.id.editReview);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        writeReviewLay = (LinearLayout) findViewById(R.id.writeReviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        reviewDate = (TextView) findViewById(R.id.reviewDate);

        topPadding = TextbookTakeoverApplication.dpToPx(OrderDetail.this, 5);
        leftPadding = TextbookTakeoverApplication.dpToPx(OrderDetail.this, 15);

        display = this.getWindowManager().getDefaultDisplay();
        data = (HashMap<String, String>) getIntent().getExtras().get("data");
        Log.v("Order", "detail" + data);
        from = (String) getIntent().getExtras().get("from");
        position = (Integer) getIntent().getExtras().get("position");

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

        backBtn.setVisibility(View.VISIBLE);
        titleimage.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        settingbtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        settingbtn.setOnClickListener(this);
        chatseller.setOnClickListener(this);
        printbtn.setOnClickListener(this);
        writeReviewLay.setOnClickListener(this);
        editReview.setOnClickListener(this);

        setData();
    }

    /**
     * To convert timestamp to Date
     **/
    private String getDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("MMM d, yyyy", getResources().getConfiguration().locale);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public class MyPrintDocumentAdapter extends PrintDocumentAdapter {


        PdfDocument pdfdocument;
        Context context;
        File file;


        private static final int MILS_IN_INCH = 1000;

        public MyPrintDocumentAdapter(File f) {
            file = f;
        }


        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback, Bundle extras) {


            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            int pages = computePageCount(newAttributes);

            PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(pdfName).
                    setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

            callback.onLayoutFinished(pdi, true);
        }


        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal cancellationSignal,
                            final WriteResultCallback callback) {

            InputStream input = null;
            OutputStream output = null;

            try {

                input = new FileInputStream(file);
                output = new FileOutputStream(destination.getFileDescriptor());

                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }

                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

            } catch (FileNotFoundException ee) {
                ee.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        private int computePageCount(PrintAttributes printAttributes) {
            int itemsPerPage = 4; // default item count for portrait mode

            PrintAttributes.MediaSize pageSize = printAttributes.getMediaSize();
            if (!pageSize.isPortrait()) {
                // Six items per page in landscape orientation
                itemsPerPage = 3;
            }

            // Determine number of print items
            int printItemCount = 5;

            return (int) Math.ceil(printItemCount / itemsPerPage);
        }
    }

    private void createPdf() {

        String filePath = null;
        String root = Environment.getExternalStorageDirectory()
                .toString();
        File newDir = new File(root + "/Docs_"
                + getString(R.string.app_name));
        if (!newDir.exists()) {
            newDir.mkdirs();
        }

        Random gen = new Random();
        int n = 10000;
        n = gen.nextInt(n);
        pdfName = "transaction-" + n + ".pdf";
        File file = new File(newDir, pdfName);
        filePath = file.getAbsolutePath();

        if (file.exists()) {
            file.delete();
            Log.v("file.exists", "file.exists");
        }
        try {

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            Rectangle rect = new Rectangle(30, 30, 550, 800);
            writer.setBoxSize("art", rect);

            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();

            addTitle(document);
            addContent(document);

            document.close();
            refreshGallery(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v("filePath", "filePath==" + filePath);
        File f = new File(filePath);
        Uri uri = Uri.fromFile(f);
        Log.v("Uri", "Uri==" + uri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PrintManager printmanager = (PrintManager) OrderDetail.this.getSystemService(Context.PRINT_SERVICE);
            String name = "CheckPrint";
            printmanager.print(name, new MyPrintDocumentAdapter(f), null);
        } else {

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_SUBJECT, "Order Detail");
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.setType("message/*");
            startActivity(Intent.createChooser(
                    email, "Send mail..."));
        }

    }

    class HeaderFooterPageEvent extends PdfPageEventHelper {

        public void onStartPage(PdfWriter writer,Document document) {
            Rectangle rect = writer.getBoxSize("art");
            String header = getString(R.string.app_name) + " | " + getString(R.string.app_name) + " - Online Classifieds Hub for Buying and Selling Used goods.";
            long unixTime = System.currentTimeMillis();
            String date = getDate(unixTime, "dd/MM/yy, h:mm a");
            ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_LEFT, new Phrase(header), rect.getLeft(), rect.getTop(), 0);
            ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_CENTER, new Phrase(date), rect.getRight(), rect.getTop(), 0);
        }
        public void onEndPage(PdfWriter writer,Document document) {
            Rectangle rect = writer.getBoxSize("art");
            String url = Constants.url + "sales";
            String page = "Page" + document.getPageNumber() + " of " + document.getPageNumber();
            ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_LEFT, new Phrase(url), rect.getLeft(), rect.getBottom(), 0);
            ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_CENTER, new Phrase(page), rect.getRight(), rect.getBottom(), 0);
        }

    }

    private void addTitle(Document document) {
        try {
            Paragraph preface = new Paragraph();
            preface.setAlignment(Element.HEADER);
            String date = "";
            if (data.get(Constants.TAG_SALEDATE) != null && !data.get(Constants.TAG_SALEDATE).equals("")) {
                long ordDate = Long.parseLong(data.get(Constants.TAG_SALEDATE)) * 1000;
                date = getDate(ordDate, "dd/MM/yyyy");
            }
            String title = "\n\n" + "Order # " + data.get(Constants.TAG_INVOICE) + " on " + date;
            preface.add(new Paragraph(title, new Font(Font.FontFamily.UNDEFINED, 18,
                    Font.BOLD)));
            document.add(preface);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    private void addContent(Document document) {
        try {
            Paragraph header = new Paragraph();
            header.add("\n" + "Payment method : " + data.get(Constants.TAG_PAYMENT_TYPE));
            header.add("\n" + "Payment status : Completed");
            document.add(header);

            document.add( Chunk.NEWLINE );
            document.add( Chunk.NEWLINE );

            LineSeparator lineSeparator = new LineSeparator();
            lineSeparator.setLineColor(new BaseColor(Color.parseColor("#d0dbe5")));
            document.add(lineSeparator);

            document.add( Chunk.NEWLINE );

            PdfPTable table = new PdfPTable(2);
            PdfPCell cellOne = new PdfPCell(new Phrase("Buyers Details", new Font(Font.FontFamily.UNDEFINED, 14,
                    Font.BOLD)));
            PdfPCell cellTwo = new PdfPCell(new Phrase("Shipping Address", new Font(Font.FontFamily.UNDEFINED, 14,
                    Font.BOLD)));

            cellOne.setBorder(Rectangle.NO_BORDER);
            cellOne.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOne.setPaddingBottom(TextbookTakeoverApplication.dpToPx(OrderDetail.this, 5));

            cellTwo.setBorder(Rectangle.NO_BORDER);
            cellTwo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOne.setPaddingBottom(TextbookTakeoverApplication.dpToPx(OrderDetail.this, 5));

            table.addCell(cellOne);
            table.addCell(cellTwo);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            document.add(table);

            PdfPTable table2 = new PdfPTable(2);
            PdfPCell cOne = new PdfPCell(new Phrase("Buyers Name", new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));
            PdfPCell cTwo = new PdfPCell(new Phrase(data.get(Constants.TAG_NAME), new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(1);

            cOne = new PdfPCell(new Phrase("Email : " + data.get(Constants.TAG_BUYER_EMAIL)));
            cTwo = new PdfPCell(new Phrase(data.get(Constants.TAG_ADDRESS1) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(2);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(data.get(Constants.TAG_ADDRESS2) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(3);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(data.get(Constants.TAG_CITY) + " - " + data.get(Constants.TAG_ZIPCODE) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(4);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(data.get(Constants.TAG_STATE) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(5);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase(data.get(Constants.TAG_COUNTRY) + " ,"));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table2.setHeaderRows(6);

            cOne = new PdfPCell(new Phrase(""));
            cTwo = new PdfPCell(new Phrase("Phone no : " + data.get(Constants.TAG_PHONE)));

            cOne.setBorder(Rectangle.NO_BORDER);
            cOne.setHorizontalAlignment(Element.ALIGN_LEFT);

            cTwo.setBorder(Rectangle.NO_BORDER);
            cTwo.setHorizontalAlignment(Element.ALIGN_LEFT);

            table2.addCell(cOne);
            table2.addCell(cTwo);
            table2.setHorizontalAlignment(Element.ALIGN_LEFT);

            document.add(table2);

            document.add( Chunk.NEWLINE );

            LineSeparator lineSeparator2 = new LineSeparator();
            lineSeparator2.setLineColor(new BaseColor(Color.parseColor("#d0dbe5")));
            document.add(lineSeparator2);

            document.add( Chunk.NEWLINE );

            PdfPTable table3 = new PdfPTable(2);
            PdfPCell c1 = new PdfPCell(new Phrase("Item Name", new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));
            PdfPCell c2 = new PdfPCell(new Phrase("Item Unitprice      " + data.get(Constants.TAG_SYMBOL) + " " + data.get(Constants.TAG_PRICE), new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setPaddingBottom(TextbookTakeoverApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);
            table3.setHeaderRows(1);

            c1 = new PdfPCell(new Phrase(data.get(Constants.TAG_ITEMNAME)));
            c2 = new PdfPCell(new Phrase("Shipping Fee        " + data.get(Constants.TAG_SYMBOL) + " " + data.get(Constants.TAG_PRICE), new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setBorder(Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setPaddingBottom(TextbookTakeoverApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);
            table3.setHeaderRows(2);

            c1 = new PdfPCell(new Phrase(""));
            c2 = new PdfPCell(new Phrase("Total Price            " + data.get(Constants.TAG_SYMBOL) + " " + data.get(Constants.TAG_TOTAL), new Font(Font.FontFamily.UNDEFINED, 12,
                    Font.BOLD)));

            c1.setBorder(Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);

            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setCellEvent(new LineCell());
            c2.setBorder(Rectangle.NO_BORDER);
            c2.setPaddingTop(TextbookTakeoverApplication.dpToPx(OrderDetail.this, 5));
            c2.setPaddingBottom(TextbookTakeoverApplication.dpToPx(OrderDetail.this, 5));

            table3.addCell(c1);
            table3.addCell(c2);
            table3.setHorizontalAlignment(Element.ALIGN_LEFT);

            document.add(table3);

        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    class LineCell implements PdfPCellEvent {

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position,
                               PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            canvas.setLineDash(0f, 0f);
            canvas.setColorStroke(BaseColor.LIGHT_GRAY);
            canvas.moveTo(position.getLeft(), position.getTop());
            canvas.lineTo(position.getRight(), position.getTop());
            canvas.moveTo(position.getLeft(), position.getBottom());
            canvas.lineTo(position.getRight(), position.getBottom());
            canvas.stroke();
        }
    }

    public static String getDate(long timeStamp, String format) {

        try {
            DateFormat sdf = new SimpleDateFormat(format);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    class changeStatus extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_ORDER_STATUS;
            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_ORDER_STATUS);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("orderid", data.get(Constants.TAG_ORDER_ID));
            req.addProperty("chstatus", params[0]);

            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            Log.v("ChangeStatus", "response" + json);

            return json;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String res) {

            try {
                JSONObject json = new JSONObject(res);
                String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (status.equalsIgnoreCase("true")) {
                    String message = DefensiveClass.optString(json, Constants.TAG_RESULT);
                    if (message.equalsIgnoreCase("Status changed to cancelled")) {
                        Toast.makeText(OrderDetail.this, getString(R.string.status_changed_to_cancelled), Toast.LENGTH_SHORT).show();
                        data.put(Constants.TAG_STATUS, "cancelled");
                        setData();
                        if (from.equals("order")) {
                            MyOrder.Orderary.get(position).put(Constants.TAG_STATUS, "cancelled");
                            MyOrder.orderAdapter.notifyDataSetChanged();
                        } else {
                            MySale.Saleary.get(position).put(Constants.TAG_STATUS, "cancelled");
                            MySale.saleAdapter.notifyDataSetChanged();
                        }
                    } else if (message.equalsIgnoreCase("Status changed to claimed")) {
                        Toast.makeText(OrderDetail.this, getString(R.string.status_changed_to_claimed), Toast.LENGTH_SHORT).show();
                        if (from.equals("order")) {
                            data.put(Constants.TAG_STATUS, "shipped");
                            setData();
                            MyOrder.Orderary.get(position).put(Constants.TAG_STATUS, "shipped");
                            MyOrder.orderAdapter.notifyDataSetChanged();
                        } else {
                            data.put(Constants.TAG_STATUS, "claimed");
                            setData();
                            MySale.Saleary.get(position).put(Constants.TAG_STATUS, "claimed");
                            MySale.saleAdapter.notifyDataSetChanged();
                        }
                    } else if (message.equalsIgnoreCase("Status changed to Processing")) {
                        Toast.makeText(OrderDetail.this, getString(R.string.status_changed_to_processing), Toast.LENGTH_SHORT).show();
                        data.put(Constants.TAG_STATUS, "processing");
                        setData();
                        if (from.equals("order")) {
                            MyOrder.Orderary.get(position).put(Constants.TAG_STATUS, "processing");
                            MyOrder.orderAdapter.notifyDataSetChanged();
                        } else {
                            MySale.Saleary.get(position).put(Constants.TAG_STATUS, "processing");
                            MySale.saleAdapter.notifyDataSetChanged();
                        }
                    } else if (message.equalsIgnoreCase("Status changed to delivered")) {
                        Toast.makeText(OrderDetail.this, getString(R.string.status_changed_to_delivered), Toast.LENGTH_SHORT).show();
                        data.put(Constants.TAG_STATUS, "delivered");
                        setData();
                        if (from.equals("order")) {
                            MyOrder.Orderary.get(position).put(Constants.TAG_STATUS, "delivered");
                            MyOrder.orderAdapter.notifyDataSetChanged();
                        } else {
                            MySale.Saleary.get(position).put(Constants.TAG_STATUS, "delivered");
                            MySale.saleAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Toast.makeText(OrderDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setData() {
        Picasso.with(OrderDetail.this).load(data.get(Constants.TAG_ORDERIMG)).into(itemImage);
        itemName.setText(data.get(Constants.TAG_ITEMNAME));
        itemPrice.setText(data.get(Constants.TAG_SYMBOL) + data.get(Constants.TAG_PRICE));
        orderid.setText(data.get(Constants.TAG_ORDER_ID));
        paymenttype.setText(data.get(Constants.TAG_PAYMENT_TYPE));
        transactionid.setText(data.get(Constants.TAG_TRANSACTION_ID));
        if (from.equals("order")) {
            printbtn.setVisibility(View.GONE);
            title.setText(data.get(Constants.TAG_SELLERNAME));
            Picasso.with(OrderDetail.this).load(data.get(Constants.TAG_SELLERIMG)).into(titleimage);
            chatseller.setText(getString(R.string.chat_with_seller));
            sellername.setText(data.get(Constants.TAG_SELLERNAME));
            if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("processing") &&
                    data.get(Constants.TAG_CANCEL).equalsIgnoreCase("false")){
                settingbtn.setVisibility(View.GONE);
            }
        } else {
            title.setText(data.get(Constants.TAG_BUYERNAME));
            Picasso.with(OrderDetail.this).load(data.get(Constants.TAG_BUYERIMG)).into(titleimage);
            printbtn.setVisibility(View.VISIBLE);
            chatseller.setText(getString(R.string.chat_with_buyer));
            selrtext.setText(getString(R.string.buyer_name));
            sellername.setText(data.get(Constants.TAG_BUYERNAME));
        }
        if (data.get(Constants.TAG_STATUS).equals("cancelled")) {
            settingbtn.setVisibility(View.GONE);
            printbtn.setVisibility(View.GONE);
        }

        if (TextbookTakeoverApplication.isRTL(OrderDetail.this)){
            itemName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            itemName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

        try {
            if (data.get(Constants.TAG_SALEDATE) != null && !data.get(Constants.TAG_SALEDATE).equals("")) {
                long ordDate = Long.parseLong(data.get(Constants.TAG_SALEDATE)) * 1000;
                odate = getDate(ordDate);
            }
            if (data.get(Constants.TAG_SHIPPING_DATE) != null && !data.get(Constants.TAG_SHIPPING_DATE).equals("") &&
                    !data.get(Constants.TAG_SHIPPING_DATE).equals("0") && !data.get(Constants.TAG_STATUS).equalsIgnoreCase("processing") &&
                    !data.get(Constants.TAG_STATUS).equalsIgnoreCase("pending")) {
                long shipDate = Long.parseLong(data.get(Constants.TAG_SHIPPING_DATE)) * 1000;
                sdate = getDate(shipDate);
            } else {
                shippedDateTxt.setVisibility(View.GONE);
                shipdate.setVisibility(View.GONE);
                deliveryDateTxt.setVisibility(View.GONE);
                deliverydate.setVisibility(View.GONE);
            }
            if (data.get(Constants.TAG_DELIVERY_DATE) != null && !data.get(Constants.TAG_DELIVERY_DATE).equals("") &&
                    !data.get(Constants.TAG_DELIVERY_DATE).equals("0") && !data.get(Constants.TAG_STATUS).equalsIgnoreCase("processing") &&
                    !data.get(Constants.TAG_STATUS).equalsIgnoreCase("pending") && !data.get(Constants.TAG_STATUS).equalsIgnoreCase("shipped")) {
                long delDate = Long.parseLong(data.get(Constants.TAG_DELIVERY_DATE)) * 1000;
                ddate = getDate(delDate);
            } else {
                deliveryDateTxt.setVisibility(View.GONE);
                deliverydate.setVisibility(View.GONE);
            }
            orderdate.setText(odate);
            shipdate.setText(sdate);
            deliverydate.setText(ddate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        price.setText(data.get(Constants.TAG_SYMBOL) + data.get(Constants.TAG_PRICE));
        shipingprice.setText(data.get(Constants.TAG_SYMBOL) + data.get(Constants.TAG_SHIPPING_COST));
        total.setText(data.get(Constants.TAG_SYMBOL) + data.get(Constants.TAG_TOTAL));
        nickName.setText(data.get(Constants.TAG_NICKNAME));
        fullName.setText(data.get(Constants.TAG_NAME));
        addressLine.setText(data.get(Constants.TAG_ADDRESS1)+" , "+ data.get(Constants.TAG_ADDRESS2));
        city.setText(data.get(Constants.TAG_CITY)+" , "+ data.get(Constants.TAG_ZIPCODE));
        country.setText(data.get(Constants.TAG_COUNTRY));

        if (from.equals("sale") || data.get(Constants.TAG_REVIEW_ID).equals("0")){
            reviewLay.setVisibility(View.GONE);
        } else {
            reviewLay.setVisibility(View.VISIBLE);
            reviewTitle.setText(data.get(Constants.TAG_REVIEW_TITLE));
            review.setText(data.get(Constants.TAG_REVIEW_DES));
            try {
                ratingBar.setRating(Float.parseFloat(data.get(Constants.TAG_RATING)));
                ratingCount.setText("("+data.get(Constants.TAG_RATING)+")");
                long date = Long.parseLong(data.get(Constants.TAG_CREATED_DATE)) * 1000;
                reviewDate.setText(getString(R.string.on) + " " + getDate(date));
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (NumberFormatException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        if (data.get(Constants.TAG_STATUS).equals("pending")) {
            orderstatus.setText(getString(R.string.pending));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                orderstatus.setBackground(getResources().getDrawable(R.drawable.dark_round_corner));
            } else {
                orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_round_corner));
            }
        } else if (data.get(Constants.TAG_STATUS).equals("processing")){
            orderstatus.setText(getString(R.string.under_processing));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                orderstatus.setBackground(getResources().getDrawable(R.drawable.dark_round_corner));
            } else {
                orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_round_corner));
            }
        } else if (data.get(Constants.TAG_STATUS).equals("cancelled")) {
            orderstatus.setText(getString(R.string.order_canceled));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                orderstatus.setBackground(getResources().getDrawable(R.drawable.red_round_corner));
            } else {
                orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_round_corner));
            }
        } else if (data.get(Constants.TAG_STATUS).equals("claimed")) {
            orderstatus.setText(getString(R.string.claimed));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                orderstatus.setBackground(getResources().getDrawable(R.drawable.blue_round_corner));
            } else {
                orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_round_corner));
            }
        } else if (data.get(Constants.TAG_STATUS).equals("delivered")) {
            orderstatus.setText(getString(R.string.item_delivered));
            if (data.get(Constants.TAG_REVIEW_ID).equals("0") && from.equals("order")){
                writeReviewLay.setVisibility(View.VISIBLE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                orderstatus.setBackground(getResources().getDrawable(R.drawable.blue_round_corner));
            } else {
                orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_round_corner));
            }
        } else if (data.get(Constants.TAG_STATUS).equals("paid")) {
            if (from.equals("sale")) {
                orderstatus.setText(getString(R.string.paid));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    orderstatus.setBackground(getResources().getDrawable(R.drawable.blue_round_corner));
                } else {
                    orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_round_corner));
                }
            } else {
                orderstatus.setText(getString(R.string.item_delivered));
                if (data.get(Constants.TAG_REVIEW_ID).equals("0") && from.equals("order")){
                    writeReviewLay.setVisibility(View.VISIBLE);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    orderstatus.setBackground(getResources().getDrawable(R.drawable.blue_round_corner));
                } else {
                    orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_round_corner));
                }
            }
        }else if (data.get(Constants.TAG_STATUS).equals("shipped")) {
            orderstatus.setText(getString(R.string.item_shipped));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                orderstatus.setBackground(getResources().getDrawable(R.drawable.orang_round_corner));
            } else {
                orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.orang_round_corner));
            }
        }

        orderstatus.setPadding(leftPadding, topPadding, leftPadding, topPadding);
    }

    /**
     * for show the popupmenu window
     **/
    public void shareImage(View v) {
        String[] values = {""};
        try {
            if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("pending")) {
                if (from.equals("sale")) {
                    values = new String[]{getString(R.string.mark_as_processing)};
                } else if (data.get(Constants.TAG_CANCEL).equalsIgnoreCase("true")){
                    values = new String[]{getString(R.string.cancel_your_order)};
                }
            } else if(data.get(Constants.TAG_STATUS).equalsIgnoreCase("processing")){
                if (from.equals("sale")) {
                    values = new String[]{getString(R.string.mark_as_shipped)};
                } else if (data.get(Constants.TAG_CANCEL).equalsIgnoreCase("true")) {
                    values = new String[]{getString(R.string.cancel_your_order)};
                }
            } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("shipped")) {
                if (from.equals("sale")) {
                    if (data.get(Constants.TAG_CLAIM).equalsIgnoreCase("true")) {
                        values = new String[]{getString(R.string.edit_tracking), getString(R.string.mark_as_claimed)};
                    } else {
                        values = new String[]{getString(R.string.edit_tracking)};
                    }
                } else {
                    values = new String[]{getString(R.string.shipping_details), getString(R.string.mark_as_received)};
                }
            } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("delivered")) {
                if (from.equals("sale") && data.get(Constants.TAG_CLAIM).equals("true")) {
                    values = new String[]{getString(R.string.shipping_details), getString(R.string.mark_as_claimed)};
                } else {
                    values = new String[]{getString(R.string.shipping_details)};
                }
            } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("claimed")) {
                if (from.equals("sale")) {
                    values = new String[]{getString(R.string.shipping_details)};
                } else {
                    values = new String[]{getString(R.string.shipping_details), getString(R.string.mark_as_received)};
                }
            } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("paid")) {
                if (from.equals("sale")) {
                    values = new String[]{getString(R.string.shipping_details)};
                } else {
                    values = new String[]{getString(R.string.shipping_details)};
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(OrderDetail.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP | Gravity.RIGHT, 0, 20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int pos, long id) {
                switch (pos) {
                    case 0:
                        if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("pending") && from.equals("sale")) {
                            confirmdialog(getString(R.string.processing_confirmation), "Processing");
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("pending") && data.get(Constants.TAG_CANCEL).equalsIgnoreCase("true")) {
                            confirmdialog(getString(R.string.cancel_confirmation), "cancel");
                        } else if(data.get(Constants.TAG_STATUS).equalsIgnoreCase("processing") && from.equals("sale")){
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra("data", data);
                            j.putExtra("from", from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("processing") && data.get(Constants.TAG_CANCEL).equalsIgnoreCase("true")) {
                            confirmdialog(getString(R.string.cancel_confirmation), "cancel");
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("shipped") && from.equals("sale")) {
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra("data", data);
                            j.putExtra("from", from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("shipped")) {
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra("data", data);
                            j.putExtra("from", from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("delivered")) {
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra("data", data);
                            j.putExtra("from", from);
                            j.putExtra("position", position);
                            startActivity(j);
                        }  else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("claimed") && from.equals("sale")) {
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra("data", data);
                            j.putExtra("from", from);
                            j.putExtra("position", position);
                            startActivity(j);
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("claimed") ||
                                data.get(Constants.TAG_STATUS).equalsIgnoreCase("paid")) {
                            Intent j = new Intent(OrderDetail.this, ShippingDetail.class);
                            j.putExtra("data", data);
                            j.putExtra("from", from);
                            j.putExtra("position", position);
                            startActivity(j);
                        }
                        popup.dismiss();
                        break;
                    case 1:
                        if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("shipped") && from.equals("sale") &&
                                data.get(Constants.TAG_CLAIM).equalsIgnoreCase("true")) {
                            confirmdialog(getString(R.string.claim_confirmation), "claim");
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("shipped")) {
                            confirmdialog(getString(R.string.deliver_confirmation), "Delivered");
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("delivered") && from.equals("sale") &&
                                data.get(Constants.TAG_CLAIM).equalsIgnoreCase("true")) {
                            confirmdialog(getString(R.string.claim_confirmation), "claim");
                        } else if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("claimed") && from.equals("order")) {
                            confirmdialog(getString(R.string.deliver_confirmation), "Delivered");
                        }
                        popup.dismiss();
                        break;
                }
            }
        });
    }

    class getChatId extends AsyncTask<String, String, Void> {
        private ProgressDialog dialog = new ProgressDialog(OrderDetail.this);

        @Override
        protected Void doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_CHAT_ID;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_CHAT_ID);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("sender_id", GetSet.getUserId());
            if (from.equals("order")) {
                req.addProperty("receiver_id", data.get(Constants.TAG_SELLERID));
            } else {
                req.addProperty("receiver_id", data.get(Constants.TAG_BUYERID));
            }

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            try {
                JSONObject jobj = new JSONObject(json);
                String status = jobj.getString(Constants.TAG_STATUS);

                if (status.equals("true")) {
                    chatId = jobj.getString("chat_id");
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage(getString(R.string.pleasewait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            chatseller.setOnClickListener(OrderDetail.this);
            if (this.dialog.isShowing() && this.dialog != null) {
                this.dialog.dismiss();
            }
            Intent i = new Intent(OrderDetail.this, ChatActivity.class);
            if (from.equals("order")) {
                Log.v("userName", "userName="+data.get(Constants.TAG_SELLERNAME));
                Log.v("userImage", "userImage="+data.get(Constants.TAG_SELLERIMG));
                i.putExtra("userName", data.get(Constants.TAG_SELLER_USERNAME));
                i.putExtra("userId", data.get(Constants.TAG_SELLERID));
                i.putExtra("userImage", data.get(Constants.TAG_SELLERIMG));
                i.putExtra("fullName", data.get(Constants.TAG_SELLERNAME));
            } else {
                i.putExtra("userName", data.get(Constants.TAG_BUYER_USERNAME));
                i.putExtra("userId", data.get(Constants.TAG_BUYERID));
                i.putExtra("userImage", data.get(Constants.TAG_BUYERIMG));
                i.putExtra("fullName", data.get(Constants.TAG_BUYERNAME));
            }
            i.putExtra("chatId", chatId);
            i.putExtra("from", "orders");
            startActivity(i);
        }
    }

    public void confirmdialog(final String title, final String orderStatus) {
        final Dialog dialog = new Dialog(OrderDetail.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(display.getWidth()*90/100, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(title);

        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new changeStatus().execute(orderStatus);

            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.settingbtn:
                shareImage(v);
                break;
            case R.id.chatseller:
                if (TextbookTakeoverApplication.isNetworkAvailable(OrderDetail.this)) {
                    chatseller.setOnClickListener(null);
                    new getChatId().execute("chat");
                } else {
                    TextbookTakeoverApplication.dialog(OrderDetail.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                }
                break;
            case R.id.printbtn:
                createPdf();
                break;
            case R.id.writeReviewLay:
                Intent l = new Intent(OrderDetail.this, WriteReview.class);
                l.putExtra("from", "write");
                l.putExtra("data", data);
                l.putExtra("position", position);
                startActivity(l);
                break;
            case R.id.editReview:
                Intent k = new Intent(OrderDetail.this, WriteReview.class);
                k.putExtra("from", "edit");
                k.putExtra("data", data);
                k.putExtra("position", position);
                startActivity(k);
                break;
        }
    }
}