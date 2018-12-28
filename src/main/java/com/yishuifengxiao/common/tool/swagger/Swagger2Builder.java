package com.yishuifengxiao.common.tool.swagger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import springfox.documentation.schema.Annotations;
import springfox.documentation.service.ObjectVendorExtension;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * 基于Swagger的拓展,这里只是为了让swagger支持extensions拓展属性
 * 
 * @author yishui
 * @date 2018年12月5日
 * @Version 0.0.1
 */
public class Swagger2Builder implements ModelPropertyBuilderPlugin {
	private final static Logger log = LoggerFactory.getLogger(Swagger2Builder.class);
	private final DescriptionResolver descriptions;

	public Swagger2Builder(DescriptionResolver descriptions) {
		this.descriptions = descriptions;
	}
   @Override
	public void apply(ModelPropertyContext context) {
		Optional<ApiModelProperty> annotation = Optional.absent();

		if (context.getBeanPropertyDefinition().isPresent()) {
			annotation = annotation.or(Annotations.findPropertyAnnotation(
					(BeanPropertyDefinition) context.getBeanPropertyDefinition().get(), ApiModelProperty.class));
		}

		if (annotation.isPresent()) {
			context.getBuilder().extensions(annotation.transform(toExtension()).orNull());
		}

	}

	/**
	 * @return the descriptions
	 */
	public DescriptionResolver getDescriptions() {
		return descriptions;
	}
	@Override
	public boolean supports(DocumentationType delimiter) {
		return SwaggerPluginSupport.pluginDoesApply(delimiter);
	}

	@SuppressWarnings("rawtypes")
	static Function<ApiModelProperty, List<VendorExtension>> toExtension() {
		return new Function<ApiModelProperty, List<VendorExtension>>() {
			@Override
			public List<VendorExtension> apply(ApiModelProperty annotation) {
				log.debug("==========================> 获取到的ApiModelProperty 属性的名字为 {}", annotation);
				Extension[] extensions = annotation.extensions();
				List<VendorExtension> list = new ArrayList<>();
				if (extensions != null && extensions.length > 0) {
					for (int i = 0; i < extensions.length; i++) {

						Extension extension = extensions[i];
						String name = extension.name();
						ObjectVendorExtension objectVendorExtension = new ObjectVendorExtension(name);
						if (!"".equals(name)) {
							for (int j = 0; j < extension.properties().length; j++) {
								ExtensionProperty extensionProperty = extension.properties()[j];
								StringVendorExtension stringVendorExtension = new StringVendorExtension(
										extensionProperty.name(), extensionProperty.value());
								objectVendorExtension.addProperty(stringVendorExtension);
							}
							list.add(objectVendorExtension);
						}
					}
				}
				return list;
			}
		};
	}

}